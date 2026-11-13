package baitolog.student;

import java.time.YearMonth;

import bean.Report;
import bean.Shift;
import bean.Student;
import bean.User;
import dao.ReportDao;
import dao.ShiftDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tool.Action;

/**
 * シフトを削除する
 */
public class ShiftDeleteExecuteAction extends Action {

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		HttpSession session = req.getSession(false);
		if (session == null) {
			res.sendRedirect(req.getContextPath() + "/baitolog/Login.action");
			return;
		}
		User user = (User) session.getAttribute("user");
		if (!(user instanceof Student) || !user.isAuthenticated()) {
			res.sendRedirect(req.getContextPath() + "/baitolog/Login.action");
			return;
		}
		Student student = (Student) user;

		// ---------- パラメータ取得 ----------
		String idText = req.getParameter("id");
		String monthParam = req.getParameter("month");

		if (idText == null || idText.isBlank()) {
			session.setAttribute("errorMessage", "削除対象が指定されていません。");
			redirectToEdit(res, req, monthParam);
			return;
		}

		int id;
		try {
			id = Integer.parseInt(idText);
		} catch (NumberFormatException e) {
			session.setAttribute("errorMessage", "削除対象が正しくありません。");
			redirectToEdit(res, req, monthParam);
			return;
		}

		// ---------- シフト取得（本人のものか確認） ----------
		ShiftDao shiftDao = new ShiftDao();
		Shift shift = shiftDao.get(id, student);
		if (shift == null) {
			session.setAttribute("errorMessage", "削除できません。");
			redirectToEdit(res, req, monthParam);
			return;
		}

		// ---------- 提出済みの月は削除不可 ----------
		ReportDao reportDao = new ReportDao();
		Report report = reportDao.get(student, shift.getWorkDate());
		if (report != null && report.isSubmitted()) {
			session.setAttribute("errorMessage", "提出済みの月は削除できません。");
			redirectToEdit(res, req, monthParam);
			return;
		}

		// ---------- 削除実行 ----------
		boolean deleted = shiftDao.delete(shift);
		if (!deleted) {
			session.setAttribute("errorMessage", "削除に失敗しました。");
			redirectToEdit(res, req, monthParam);
			return;
		}

		// ---------- 成功メッセージをセットしてリダイレクト ----------
		session.setAttribute("message", "削除しました");
		String month = (monthParam != null && !monthParam.isBlank())
				? monthParam
				: YearMonth.from(shift.getWorkDate()).toString();
		res.sendRedirect(req.getContextPath() + "/baitolog/student/ShiftEdit.action?month=" + month);
	}

	private void redirectToEdit(HttpServletResponse res, HttpServletRequest req, String monthParam)
			throws Exception {
		String month = (monthParam != null && !monthParam.isBlank())
				? monthParam
				: YearMonth.now().toString();
		res.sendRedirect(req.getContextPath() + "/baitolog/student/ShiftEdit.action?month=" + month);
	}
}