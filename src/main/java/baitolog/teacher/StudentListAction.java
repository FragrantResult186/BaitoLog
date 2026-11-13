package baitolog.teacher;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.JudgeSummary;
import bean.LongBreak;
import bean.School;
import bean.SchoolClass;
import bean.Shift;
import bean.Student;
import bean.StudentSummary;
import bean.Teacher;
import bean.User;
import bean.WindowResult;
import bean.Workplace;
import dao.LongBreakDao;
import dao.SchoolClassDao;
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
 * 学生一覧（教職員のトップ画面）
 * 月とクラスを選んで、学生ごとの「7日間の最大の労働時間」と状態（超過／間近／上限内）を見る。
 * 一番時間が多い学生が上に並ぶ。学生の氏名を押すと、学生別記録へ進む
 *
 * GET ?target_month=yyyy-MM（対象期間。なければ今月） &class_id=（クラス。なければすべて）
 *     &csv=1（絞り込んだ一覧をCSVで出力）
 */
public class StudentListAction extends Action {

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		//ローカル変数の宣言 1
		String root = req.getContextPath() + "/baitolog";
		String errorMessage = "";
		List<Student> students = new ArrayList<>();
		List<SchoolClass> classes = new ArrayList<>();
		List<StudentSummary> summaries = new ArrayList<>();
		SchoolClass selectedClass = null;
		StudentDao studentDao = new StudentDao();
		SchoolClassDao schoolClassDao = new SchoolClassDao();
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
		YearMonth month = TargetMonth.parse(req.getParameter("target_month"), today);
		String classIdText = req.getParameter("class_id");
		boolean isCsv = "1".equals(req.getParameter("csv"));
		LocalDate monthStart = month.atDay(1);
		LocalDate monthEnd = month.atEndOfMonth();

		//DBからデータ取得 3
		classes = schoolClassDao.filter(school);

		//ビジネスロジック 4
		// 絞り込むクラス（自分の学校のクラスだけ選べる）
		if (classIdText != null && !classIdText.isEmpty()) {
			for (SchoolClass schoolClass : classes) {
				if (String.valueOf(schoolClass.getId()).equals(classIdText)) {
					selectedClass = schoolClass;
				}
			}
			if (selectedClass == null) {
				errorMessage = "選んだクラスが見つかりません";
			}
		}
		// クラスは「月初の日」の在籍で見る（クラス替えをした学生は、その月の最初のクラスに出る）
		if (selectedClass != null) {
			students = studentDao.filter(school, selectedClass, monthStart);
		} else {
			students = studentDao.filter(school, monthStart);
		}

		// 7日間の判定には、月初の6日前からのシフトが必要
		LocalDate shiftStart = monthStart.minusDays(6);
		List<Shift> allShifts = shiftDao.filter(students, shiftStart, monthEnd);
		List<LongBreak> longBreaks = longBreakDao.filter(school, shiftStart, monthEnd);
		LimitPolicy policy = new LimitPolicy(school, longBreaks);

		// 学生ID → シフト
		Map<Integer, List<Shift>> shiftMap = new HashMap<>();
		for (Shift shift : allShifts) {
			shiftMap.computeIfAbsent(shift.getStudentId(), k -> new ArrayList<>()).add(shift);
		}

		int cautionCount = 0;
		int overCount = 0;
		int noneCount = 0;
		for (Student student : students) {
			List<Shift> shifts = shiftMap.getOrDefault(student.getId(), new ArrayList<>());
			List<Workplace> workplaces = workplaceDao.findByStudent(student.getId());

			StudentSummary summary = new StudentSummary();
			summary.setStudent(student);
			summary.setWorkplaceCount(workplaces.size());
			// その月にシフトがあれば「入力あり」（前月の分だけでは、入力ありにしない）
			for (Shift shift : shifts) {
				if (!shift.getWorkDate().isBefore(monthStart)) {
					summary.setEntered(true);
					break;
				}
			}
			if (summary.isEntered()) {
				JudgeSummary judged = judge.judgeMonth(shifts, workplaces, monthStart, monthEnd, policy);
				summary.setMaxWindow(judged.getMaxWindow());
			}

			String status = summary.getStatus();
			if (WindowResult.STATUS_OVER.equals(status)) {
				overCount++;
			} else if (WindowResult.STATUS_CAUTION.equals(status)) {
				cautionCount++;
			} else if (StudentSummary.STATUS_NONE.equals(status)) {
				noneCount++;
			}
			summaries.add(summary);
		}

		// 7日間の最大が多い順（未入力は一番下。同じなら学生番号の順）
		summaries.sort(Comparator
				.comparingDouble((StudentSummary s) -> s.isEntered() ? -s.getMaxWindow().getTotalHours() : 1)
				.thenComparing(s -> s.getStudent().getLoginId()));

		//DBへデータ保存 5
		// 一覧は見るだけなので、保存はしない

		//CSV出力（絞り込んだ一覧をそのまま出す）
		if (isCsv) {
			writeCsv(res, summaries, month);
			return;
		}

		//レスポンス値をセット 6
		req.setAttribute("summaries", summaries);
		req.setAttribute("classes", classes);
		req.setAttribute("months", TargetMonth.options(today));
		req.setAttribute("targetMonth", month.toString());
		req.setAttribute("selectedClassId", selectedClass == null ? "" : String.valueOf(selectedClass.getId()));
		req.setAttribute("studentCount", students.size());
		req.setAttribute("cautionCount", cautionCount);
		req.setAttribute("overCount", overCount);
		req.setAttribute("noneCount", noneCount);
		req.setAttribute("errorMessage", errorMessage);

		//JSPへフォワード 7
		req.getRequestDispatcher("student_list.jsp").forward(req, res);
	}

	/**
	 * writeCsvメソッド 一覧をCSVで返す（Excelで開けるよう、先頭にBOMをつける）
	 */
	private void writeCsv(HttpServletResponse res, List<StudentSummary> summaries, YearMonth month) throws Exception {
		DateTimeFormatter md = DateTimeFormatter.ofPattern("M/d");
		res.setContentType("text/csv; charset=UTF-8");
		res.setHeader("Content-Disposition", "attachment; filename=\"student_list_" + month + ".csv\"");
		PrintWriter out = res.getWriter();
		out.print("﻿");
		out.print("学生番号,氏名,クラス,勤務先数,7日間の最大(時間),該当期間,状態\r\n");
		for (StudentSummary s : summaries) {
			Student student = s.getStudent();
			WindowResult w = s.getMaxWindow();
			out.print(csv(student.getLoginId()) + ",");
			out.print(csv(student.getName()) + ",");
			out.print(csv(student.getSchoolClass() == null ? "" : student.getSchoolClass().getName()) + ",");
			out.print(s.getWorkplaceCount() + ",");
			out.print((w == null ? "未入力" : String.format("%.1f", w.getTotalHours())) + ",");
			out.print((w == null ? "" : w.getStartDate().format(md) + "～" + w.getEndDate().format(md)) + ",");
			out.print(statusText(s.getStatus()) + "\r\n");
		}
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

	private String statusText(String status) {
		if (WindowResult.STATUS_OVER.equals(status)) {
			return "超過";
		}
		if (WindowResult.STATUS_CAUTION.equals(status)) {
			return "間近";
		}
		if (WindowResult.STATUS_OK.equals(status)) {
			return "上限内";
		}
		return "ー";
	}
}
