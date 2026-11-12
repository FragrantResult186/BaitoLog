package baitolog.student;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import bean.Report;
import bean.Student;
import bean.User;
import dao.ReportDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import logic.TargetMonth;
import tool.Action;

/**
 * 提出済みの報告書を見る画面（学生）
 */
public class ReportAction extends Action {

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		// 1. ローカル変数の宣言
		HttpSession session = req.getSession();
		String root = req.getContextPath() + "/baitolog";
		ReportDao reportDao = new ReportDao();

		User user = (User) session.getAttribute("user");
		if (!(user instanceof Student) || !user.isAuthenticated()) {
			res.sendRedirect(root + "/Login.action");
			return;
		}
		Student student = (Student) user;

		// 2. リクエストパラメータの取得
		YearMonth targetMonth = TargetMonth.parse(req.getParameter("month"), LocalDate.now());

		// 3. DBからデータ取得
		Report report = reportDao.get(student, targetMonth.atDay(1));

		// 4. ビジネスロジック
		// まだ提出していない月は、提出確認の画面へ
		if (report == null || !report.isSubmitted()) {
			res.sendRedirect(root + "/student/ReportConfirm.action?month=" + targetMonth);
			return;
		}

		// 5. DBへデータ保存
		// なし

		// 6. レスポンス値をセット
		req.setAttribute("report", report);
		req.setAttribute("month", targetMonth.toString());
		req.setAttribute("monthLabel", targetMonth.getYear() + "年" + targetMonth.getMonthValue() + "月");
		req.setAttribute("submittedAtText", report.getSubmittedAt().format(DateTimeFormatter.ofPattern("yyyy/M/d HH:mm")));

		// 7. JSPへフォワード
		req.getRequestDispatcher("report.jsp").forward(req, res);
	}
}
