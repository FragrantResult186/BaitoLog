package baitolog.teacher;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import bean.DayRecord;
import bean.JudgeSummary;
import bean.LongBreak;
import bean.School;
import bean.Shift;
import bean.Student;
import bean.Teacher;
import bean.User;
import bean.WindowResult;
import bean.Workplace;
import dao.LongBreakDao;
import dao.ShiftDao;
import dao.StudentDao;
import dao.WorkplaceDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import logic.LimitPolicy;
import logic.SevenDayJudge;
import logic.TargetMonth;
import tool.Action;

/**
 * 学生別記録（教職員）
 * 1人の学生の、選んだ月の勤務記録を見る。画面の上から、
 *   ・7日間の合計と判定（初めは、その月で一番重い7日間。日別グラフの日付を押すと、その日を中心にした7日間へ移る）
 *   ・日別の労働時間のグラフ
 *   ・7日間の日別勤務記録（CSV出力できる）
 *   ・パスワードのリセット（一番下）
 *
 * GET  ?id=（学生のユーザーID） &target_month=yyyy-MM &center=yyyy-MM-dd（7日間の真ん中の日） &class_id=（学生一覧へ戻るときのクラス）
 *      &csv=1（表示中の7日間の記録をCSVで出力）
 *
 * パスワードのリセットは、確認してから進む（学生のパスワードを生年月日（yyyyMMdd）に戻す。勤務先・シフトなどは消えない）
 *   1. 確認   POST reset_step=confirm                    「戻しますか？」を同じ画面に出す
 *   2. 実行   POST reset_step=execute & token=           戻して、この画面へ移動（GET）
 */
public class StudentDetailAction extends Action {

	/** セッションに置く名前（確認から実行まで、同じ学生・1回だけ使えるようにする） */
	private static final String S_TOKEN = "resetToken";
	private static final String S_ID = "resetStudentId";

	/** セッションに置く名前（リセットのあとに1回だけ表示する） */
	private static final String S_RESET_MESSAGE = "studentResetMessage";

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		//ローカル変数の宣言 1
		String root = req.getContextPath() + "/baitolog";
		String errorMessage = "";
		String resetStep = "";
		Student student = null;
		StudentDao studentDao = new StudentDao();
		ShiftDao shiftDao = new ShiftDao();
		WorkplaceDao workplaceDao = new WorkplaceDao();
		LongBreakDao longBreakDao = new LongBreakDao();
		SevenDayJudge judge = new SevenDayJudge();
		LocalDate today = LocalDate.now();

		//リクエストパラメータ―の取得 2
		HttpSession session = req.getSession(false);
		User user = session == null ? null : (User) session.getAttribute("user");
		if (user == null || !user.isAuthenticated() || !user.isTeacher() || !(user instanceof Teacher)) {
			// 教職員としてログインしていない場合は、ログイン画面へ戻す
			res.sendRedirect(root + "/Login.action");
			return;
		}
		School school = ((Teacher) user).getSchool();
		boolean isPost = "POST".equals(req.getMethod());
		int id = parseInt(req.getParameter("id"));
		YearMonth month = TargetMonth.parse(req.getParameter("target_month"), today);
		String classId = req.getParameter("class_id") == null ? "" : req.getParameter("class_id").trim();
		LocalDate center = parseDate(req.getParameter("center"));
		boolean isCsv = !isPost && "1".equals(req.getParameter("csv"));
		LocalDate monthStart = month.atDay(1);
		LocalDate monthEnd = month.atEndOfMonth();

		//DBからデータ取得 3
		// 自分の学校の学生だけ取れる（ほかの学校の学生・存在しないIDはnull）
		if (id > 0) {
			student = studentDao.get(id, school);
		}
		if (student == null) {
			// 学生が見つからないときは、学生一覧へ戻す
			res.sendRedirect(root + "/teacher/StudentList.action");
			return;
		}

		// パスワードのリセット
		boolean canReset = student.getBirthday() != null;
		String token = "";
		String backQuery = "id=" + student.getId() + "&target_month=" + month + "&class_id=" + encode(classId)
				+ (center == null ? "" : "&center=" + center);
		if (isPost) {
			String step = req.getParameter("reset_step");
			if (!canReset) {
				errorMessage = "この学生は生年月日が登録されていないため、リセットできません";
			} else if ("confirm".equals(step)) {
				resetStep = "confirm";
			} else if ("execute".equals(step)) {
				Object savedToken = session.getAttribute(S_TOKEN);
				Object savedId = session.getAttribute(S_ID);
				String postedToken = req.getParameter("token");
				// 同じ確認では二度実行できないよう、先にトークンを外す
				session.removeAttribute(S_TOKEN);
				session.removeAttribute(S_ID);
				if (savedToken == null || postedToken == null || !savedToken.equals(postedToken)
						|| !Integer.valueOf(student.getId()).equals(savedId)) {
					// 期限切れ・別の学生・二重送信
					errorMessage = "確認からやり直してください";
				} else {
					//DBへデータ保存 5
					if (studentDao.resetPassword(student)) {
						String password = student.getBirthday().format(DateTimeFormatter.BASIC_ISO_DATE);
						session.setAttribute(S_RESET_MESSAGE, "パスワードを生年月日に戻しました。初期パスワード：" + password);
						res.sendRedirect(root + "/teacher/StudentDetail.action?" + backQuery + "#reset");
						return;
					}
					errorMessage = "パスワードを戻せませんでした";
				}
			}
			if (!errorMessage.isEmpty()) {
				resetStep = "";
			}
		}
		if ("confirm".equals(resetStep)) {
			// 確認を開くたびに、新しいトークンを作る
			token = UUID.randomUUID().toString();
			session.setAttribute(S_TOKEN, token);
			session.setAttribute(S_ID, student.getId());
		}

		// 月のシフト（7日間の判定のため月初の6日前から、真ん中の日を月末に選んだときのため月末の3日後まで）
		LocalDate shiftStart = monthStart.minusDays(6);
		LocalDate shiftEnd = monthEnd.plusDays(3);
		List<Shift> shifts = shiftDao.filter(student, shiftStart, shiftEnd);
		List<Workplace> workplaces = workplaceDao.findByStudent(student.getId());
		List<LongBreak> longBreaks = longBreakDao.filter(school, shiftStart, shiftEnd);

		//ビジネスロジック 4
		LimitPolicy policy = new LimitPolicy(school, longBreaks);

		// 見る7日間を決める：真ん中の日が月の中にあれば、その日を真ん中に。なければ、その月で一番重い7日間
		// （月の7日間は、月の中の日で終わるもの。月初で終わる7日間は前月の6日分を含む）
		LocalDate windowStart;
		if (center != null && !center.isBefore(monthStart) && !center.isAfter(monthEnd)) {
			windowStart = center.minusDays(3);
		} else {
			center = null;
			windowStart = monthStart;
			boolean entered = false;
			for (Shift shift : shifts) {
				if (!shift.getWorkDate().isBefore(monthStart) && !shift.getWorkDate().isAfter(monthEnd)) {
					entered = true;
					break;
				}
			}
			if (entered) {
				JudgeSummary judged = judge.judgeMonth(shifts, workplaces, monthStart, monthEnd, policy);
				windowStart = judged.getMaxWindow().getStartDate();
			}
		}
		LocalDate windowEnd = windowStart.plusDays(6);
		WindowResult window = judge.judgeWindow(shifts, workplaces, windowStart, windowEnd, policy);

		// 日ごとにシフトをまとめる（グラフ用：月の全日、表用：見る7日間）
		Map<LocalDate, DayRecord> dayMap = new HashMap<>();
		for (LocalDate d = shiftStart; !d.isAfter(shiftEnd); d = d.plusDays(1)) {
			dayMap.put(d, new DayRecord(d));
		}
		for (Shift shift : shifts) {
			DayRecord record = dayMap.get(shift.getWorkDate());
			if (record != null) {
				record.getShifts().add(shift);
			}
		}
		List<DayRecord> monthDays = new ArrayList<>();
		for (LocalDate d = monthStart; !d.isAfter(monthEnd); d = d.plusDays(1)) {
			monthDays.add(dayMap.get(d));
		}
		List<DayRecord> windowDays = new ArrayList<>();
		for (LocalDate d = windowStart; !d.isAfter(windowEnd); d = d.plusDays(1)) {
			windowDays.add(dayMap.get(d));
		}
		// 勤務先ID → 勤務先
		Map<Integer, Workplace> workplaceMap = new HashMap<>();
		for (Workplace workplace : workplaces) {
			workplaceMap.put(workplace.getId(), workplace);
		}

		//CSV出力（見ている7日間の記録）
		if (isCsv) {
			writeCsv(res, student, windowDays, workplaceMap, windowStart);
			return;
		}

		// グラフの高さの基準（1日8時間、それより長い日があればその時間）
		double chartMax = 8;
		for (DayRecord record : monthDays) {
			chartMax = Math.max(chartMax, Math.ceil(record.getHours()));
		}

		// リセットのあとに1回だけ出すメッセージ
		Object flash = session.getAttribute(S_RESET_MESSAGE);
		if (flash != null) {
			req.setAttribute("resetMessage", flash);
			session.removeAttribute(S_RESET_MESSAGE);
		}

		//レスポンス値をセット 6
		req.setAttribute("student", student);
		req.setAttribute("workplaceCount", workplaces.size());
		req.setAttribute("months", TargetMonth.options(today));
		req.setAttribute("targetMonth", month.toString());
		req.setAttribute("classId", classId);
		req.setAttribute("window", window);
		req.setAttribute("monthDays", monthDays);
		req.setAttribute("windowDays", windowDays);
		req.setAttribute("workplaceMap", workplaceMap);
		req.setAttribute("chartMax", chartMax);
		req.setAttribute("windowStart", windowStart);
		req.setAttribute("windowEnd", windowEnd);
		req.setAttribute("center", center == null ? "" : center.toString());
		req.setAttribute("canReset", canReset);
		req.setAttribute("resetStep", resetStep);
		req.setAttribute("token", token);
		req.setAttribute("errorMessage", errorMessage);

		//JSPへフォワード 7
		req.getRequestDispatcher("student_detail.jsp").forward(req, res);
	}

	/**
	 * writeCsvメソッド 7日間の日別勤務記録をCSVで返す（Excelで開けるよう、先頭にBOMをつける）
	 */
	private void writeCsv(HttpServletResponse res, Student student, List<DayRecord> days,
			Map<Integer, Workplace> workplaceMap, LocalDate windowStart) throws Exception {
		DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm");
		String loginId = student.getLoginId().replaceAll("[^A-Za-z0-9]", "");
		res.setContentType("text/csv; charset=UTF-8");
		res.setHeader("Content-Disposition",
				"attachment; filename=\"student_" + loginId + "_" + windowStart + ".csv\"");
		PrintWriter out = res.getWriter();
		out.print("﻿");
		out.print("日付,曜日,勤務先,開始,終了,休憩開始,休憩終了,労働時間\r\n");
		double total = 0;
		for (DayRecord day : days) {
			String weekday = day.getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE);
			if (day.getShifts().isEmpty()) {
				out.print(day.getDate() + "," + weekday + ",,,,,,\r\n");
			}
			for (Shift shift : day.getShifts()) {
				Workplace workplace = workplaceMap.get(shift.getWorkplaceId());
				out.print(day.getDate() + "," + weekday + "," + csv(workplace == null ? "" : workplace.getDisplayName())
						+ "," + shift.getStartTime().format(time) + "," + shift.getEndTime().format(time) + ","
						+ (shift.hasBreak() ? shift.getBreakStart().format(time) : "") + ","
						+ (shift.hasBreak() ? shift.getBreakEnd().format(time) : "") + ","
						+ String.format("%.2f", shift.getWorkHours()) + "\r\n");
			}
			total += day.getHours();
		}
		out.print("合計,,,,,,," + String.format("%.2f", total) + "\r\n");
		out.flush();
	}

	/**
	 * csvメソッド CSVの1つ分の文字にする（カンマ・引用符・改行は引用符で囲む）
	 * 先頭が = + - @ の文字は、Excelで式として動かないよう ' をつける
	 */
	private String csv(String s) {
		if (s == null) {
			return "";
		}
		if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) {
			s = "'" + s;
		}
		if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
			s = "\"" + s.replace("\"", "\"\"") + "\"";
		}
		return s;
	}

	/** URLに入れる文字にする（クラスIDは数字だけ通す） */
	private String encode(String s) {
		return s.matches("\\d{1,9}") ? s : "";
	}

	private int parseInt(String s) {
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			return 0;
		}
	}

	private LocalDate parseDate(String s) {
		try {
			return LocalDate.parse(s.trim());
		} catch (Exception e) {
			return null;
		}
	}
}
