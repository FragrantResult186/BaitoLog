package baitolog.student;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.LongBreak;
import bean.Report;
import bean.Shift;
import bean.Student;
import bean.User;
import bean.WindowResult;
import bean.Workplace;
import dao.LongBreakDao;
import dao.ReportDao;
import dao.ShiftDao;
import dao.WorkplaceDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import logic.LimitPolicy;
import logic.SevenDayJudge;
import tool.Action;

public class StudentTopAction extends Action {

	/**
	 * 「これからのシフト」に出す件数
	 */
	private static final int UPCOMING_COUNT = 5;

	/**
	 * 「これからのシフト」を探す日数（今日から何日先まで）
	 */
	private static final int UPCOMING_DAYS = 30;

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		//ローカル変数の宣言 1
		HttpSession session = req.getSession();
		ShiftDao shiftDao = new ShiftDao();
		WorkplaceDao workplaceDao = new WorkplaceDao();
		LongBreakDao longBreakDao = new LongBreakDao();
		ReportDao reportDao = new ReportDao();
		SevenDayJudge judge = new SevenDayJudge();
		LocalDate today = LocalDate.now();
		LocalDate from = today.minusDays(6);

		//リクエストパラメータ―の取得 2
		// セッションのログインユーザー（★属性名 "user" は LoginAction に合わせること）
		User user = (User) session.getAttribute("user");
		if (!(user instanceof Student)) {
			// 未ログイン・教職員の場合はログイン画面へ
			res.sendRedirect("Login.action");
			return;
		}
		Student student = (Student) user;

		//DBからデータ取得 3
		// 7日間と「これからのシフト」をまとめて取得する
		List<Shift> shifts = shiftDao.filter(student, from, today.plusDays(UPCOMING_DAYS));
		// 本人の勤務先
		List<Workplace> workplaces = workplaceDao.findByStudent(student.getId());
		// 7日間にかかる長期休暇
		List<LongBreak> longBreaks = longBreakDao.filter(student.getSchool(), from, today);
		// 今月の報告書（なければ null ＝ 未提出）
		Report report = reportDao.get(student, today);

		//ビジネスロジック 4
		// 7日間の合計・上限・勤務先別の時間
		LimitPolicy policy = new LimitPolicy(student.getSchool(), longBreaks);
		WindowResult result = judge.judgeWindow(shifts, workplaces, from, today, policy);

		// 今日より後のシフトを、日付順に5件取り出す
		List<Shift> upcoming = new ArrayList<>();
		for (Shift shift : shifts) {
			if (shift.getWorkDate().isAfter(today)) {
				upcoming.add(shift);
				if (upcoming.size() == UPCOMING_COUNT) {
					break;
				}
			}
		}

		// 勤務先ID → 勤務先（名前も色も引ける）
		Map<Integer, Workplace> workplaceMap = new HashMap<>();
		for (Workplace workplace : workplaces) {
			workplaceMap.put(workplace.getId(), workplace);
		}

		// 報告書の提出状況
		boolean submitted = (report != null && report.isSubmitted());
		String submittedAtText = "";
		if (submitted) {
			submittedAtText = report.getSubmittedAt().format(DateTimeFormatter.ofPattern("M/d HH:mm"));
		}

		// セッションのメッセージ（提出完了など）は、取り出したら消す
		String message = (String) session.getAttribute("message");
		String errorMessage = (String) session.getAttribute("errorMessage");
		session.removeAttribute("message");
		session.removeAttribute("errorMessage");

		//DBへデータ保存 5
		//なし

		//レスポンス値をセット 6
		req.setAttribute("result", result);
		req.setAttribute("shifts", upcoming);
		req.setAttribute("workplaceMap", workplaceMap);
		req.setAttribute("submitted", submitted);
		req.setAttribute("monthLabel", today.getMonthValue() + "月");
		req.setAttribute("month", today.format(DateTimeFormatter.ofPattern("yyyy-MM")));
		req.setAttribute("submittedAtText", submittedAtText);
		req.setAttribute("message", message);
		req.setAttribute("errorMessage", errorMessage);

		//JSPへフォワード 7
		req.getRequestDispatcher("student_top.jsp").forward(req, res);
	}
}
