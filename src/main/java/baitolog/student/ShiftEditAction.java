package baitolog.student;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.Shift;
import bean.ShiftTemplate;
import bean.Student;
import bean.User;
import bean.Workplace;
import dao.ShiftDao;
import dao.ShiftTemplateDao;
import dao.WorkplaceDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tool.Action;

/**
 * シフト登録・変更画面を表示する
 */
public class ShiftEditAction extends Action {

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		// 1. ログイン確認
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

		// 2. パラメータ取得
		String monthParam = req.getParameter("month");
		String idParam = req.getParameter("id");

		YearMonth yearMonth;
		if (monthParam != null && !monthParam.isEmpty()) {
			try {
				yearMonth = YearMonth.parse(monthParam);
			} catch (java.time.format.DateTimeParseException e) {
				yearMonth = YearMonth.now();
			}
		} else {
			yearMonth = YearMonth.now();
		}

		LocalDate monthStart = yearMonth.atDay(1);
		LocalDate monthEnd = yearMonth.atEndOfMonth();

		// 3. DB取得
		ShiftDao shiftDao = new ShiftDao();
		WorkplaceDao workplaceDao = new WorkplaceDao();
		ShiftTemplateDao templateDao = new ShiftTemplateDao();

		List<Shift> shifts = shiftDao.filter(student, monthStart, monthEnd);
		List<Workplace> workplaces = workplaceDao.findByStudent(student.getId());

		// 勤務先ごとのテンプレート
		Map<Integer, List<ShiftTemplate>> templatesByWorkplace = new HashMap<>();
		for (Workplace w : workplaces) {
			templatesByWorkplace.put(w.getId(), templateDao.findByWorkplace(w.getId()));
		}

		// 編集対象シフト（id指定時）
		Shift editShift = null;
		if (idParam != null && !idParam.isEmpty()) {
			try {
				int id = Integer.parseInt(idParam);
				editShift = shiftDao.get(id, student);
				if (editShift != null) {
					yearMonth = YearMonth.from(editShift.getWorkDate());
					monthStart = yearMonth.atDay(1);
					monthEnd = yearMonth.atEndOfMonth();
					// 月が変わった場合は再取得
					shifts = shiftDao.filter(student, monthStart, monthEnd);
				}
			} catch (NumberFormatException e) {
				// 無視
			}
		}

		// 勤務先マップ（色・名前用）
		Map<Integer, Workplace> workplaceMap = new HashMap<>();
		for (Workplace w : workplaces) {
			workplaceMap.put(w.getId(), w);
		}

		// セッションメッセージ
		String message = (String) session.getAttribute("message");
		String errorMessage = (String) session.getAttribute("errorMessage");
		session.removeAttribute("message");
		session.removeAttribute("errorMessage");

		// 6. レスポンスセット
		req.setAttribute("yearMonth", yearMonth);
		req.setAttribute("monthLabel", yearMonth.format(DateTimeFormatter.ofPattern("yyyy年M月")));
		req.setAttribute("monthParam", yearMonth.toString()); // yyyy-MM
		req.setAttribute("shifts", shifts);
		req.setAttribute("workplaces", workplaces);
		req.setAttribute("workplaceMap", workplaceMap);
		req.setAttribute("templatesByWorkplace", templatesByWorkplace);
		req.setAttribute("editShift", editShift);
		req.setAttribute("message", message);
		req.setAttribute("errorMessage", errorMessage);

		// 前月・翌月リンク用
		req.setAttribute("prevMonth", yearMonth.minusMonths(1).toString());
		req.setAttribute("nextMonth", yearMonth.plusMonths(1).toString());

		// 7. フォワード
		req.getRequestDispatcher("shift_edit.jsp").forward(req, res);
	}
}