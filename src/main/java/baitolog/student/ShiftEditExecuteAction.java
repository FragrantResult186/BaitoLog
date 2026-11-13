package baitolog.student;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

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

/**
 * シフトの登録・更新を実行する
 */
public class ShiftEditExecuteAction extends Action {

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

		// ---------- 2. パラメータ取得 ----------
		String idText = req.getParameter("id");
		String workplaceIdText = req.getParameter("workplace_id");
		String workDateText = req.getParameter("work_date");
		String startTimeText = req.getParameter("start_time");
		String endTimeText = req.getParameter("end_time");
		String breakStartText = req.getParameter("break_start");
		String breakEndText = req.getParameter("break_end");
		String monthParam = req.getParameter("month"); // 戻り先の月

		int id = 0;
		int workplaceId;
		LocalDate workDate;
		LocalTime startTime;
		LocalTime endTime;
		LocalTime breakStart = null;
		LocalTime breakEnd = null;

		try {
			if (idText != null && !idText.isBlank()) {
				id = Integer.parseInt(idText);
			}
			workplaceId = Integer.parseInt(workplaceIdText);
			workDate = LocalDate.parse(workDateText);
			startTime = LocalTime.parse(startTimeText);
			endTime = LocalTime.parse(endTimeText);
			if (breakStartText != null && !breakStartText.isBlank()) {
				breakStart = LocalTime.parse(breakStartText);
			}
			if (breakEndText != null && !breakEndText.isBlank()) {
				breakEnd = LocalTime.parse(breakEndText);
			}
		} catch (Exception e) {
			session.setAttribute("errorMessage", "入力内容が正しくありません。日付・時刻を確認してください。");
			redirectToEdit(res, req, monthParam, id);
			return;
		}

		// ---------- 3. 勤務先が本人のものか確認 ----------
		WorkplaceDao workplaceDao = new WorkplaceDao();
		Workplace workplace = workplaceDao.findById(workplaceId, student.getId());
		if (workplace == null) {
			session.setAttribute("errorMessage", "選択した勤務先が見つかりません。");
			redirectToEdit(res, req, monthParam, id);
			return;
		}

		// ---------- 4. シフトオブジェクト作成＆簡易バリデーション ----------
		Shift shift = new Shift();
		shift.setId(id);
		shift.setStudentId(student.getId());
		shift.setWorkplaceId(workplaceId);
		shift.setWorkDate(workDate);
		shift.setStartTime(startTime);
		shift.setEndTime(endTime);
		shift.setBreakStart(breakStart);
		shift.setBreakEnd(breakEnd);

		if (startTime.equals(endTime)) {
			session.setAttribute("errorMessage", "開始時刻と終了時刻を異なる時刻にしてください。");
			redirectToEdit(res, req, monthParam, id);
			return;
		}
		if ((breakStart == null) != (breakEnd == null)) {
			session.setAttribute("errorMessage", "休憩を設定する場合は、開始と終了の両方を入力してください。");
			redirectToEdit(res, req, monthParam, id);
			return;
		}
		if (shift.getWorkHours() <= 0) {
			session.setAttribute("errorMessage", "労働時間が0時間以下になっています。");
			redirectToEdit(res, req, monthParam, id);
			return;
		}

		// ---------- 5. 報告書が提出済みの月は変更不可（変更前・変更後の両方の月を確認） ----------
		ShiftDao shiftDao = new ShiftDao();
		ReportDao reportDao = new ReportDao();
		if (id > 0) {
			Shift original = shiftDao.get(id, student);
			if (original == null) {
				session.setAttribute("errorMessage", "変更するシフトが見つかりません。");
				redirectToEdit(res, req, monthParam, 0);
				return;
			}
			Report originalReport = reportDao.get(student, original.getWorkDate());
			if (originalReport != null && originalReport.isSubmitted()) {
				session.setAttribute("errorMessage", "提出済みの月のシフトは変更できません。");
				redirectToEdit(res, req, monthParam, id);
				return;
			}
		}
		Report report = reportDao.get(student, workDate);
		if (report != null && report.isSubmitted()) {
			session.setAttribute("errorMessage", "提出済みの月は変更できません。");
			redirectToEdit(res, req, monthParam, id);
			return;
		}

		// ---------- 6. 7日間判定（サーバー側でも再実行） ----------
		LocalDate searchFrom = workDate.minusDays(6);
		LocalDate searchTo = workDate.plusDays(6);

		List<Shift> shifts = new ArrayList<>();
		for (Shift s : shiftDao.filter(student, searchFrom, searchTo)) {
			if (s.getId() != id) { // 編集中の自分は除外
				shifts.add(s);
			}
		}
		shifts.add(shift);

		LongBreakDao longBreakDao = new LongBreakDao();
		List<LongBreak> longBreaks = longBreakDao.filter(student.getSchool(), searchFrom, searchTo);
		LimitPolicy policy = new LimitPolicy(student.getSchool(), longBreaks);

		SevenDayJudge judge = new SevenDayJudge();
		WindowResult worst = null;
		double worstOver = -1;
		double worstRate = -1;

		for (int i = 0; i <= 6; i++) {
			LocalDate from = workDate.minusDays(6 - i);
			LocalDate to = from.plusDays(6);
			WindowResult result = judge.judgeWindow(shifts, null, from, to, policy);
			double over = result.getOverHours();
			double rate = result.getLimitHours() <= 0 ? 0 : result.getTotalHours() / result.getLimitHours();
			if (worst == null || over > worstOver || (over == worstOver && rate > worstRate)) {
				worst = result;
				worstOver = over;
				worstRate = rate;
			}
		}

		if (worst.isOver()) {
			session.setAttribute("errorMessage",
					"上限を超えます（" + round2(worst.getTotalHours()) + "時間）。登録できません。");
			redirectToEdit(res, req, monthParam, id);
			return;
		}

		// ---------- 7. 保存 ----------
		boolean saved = shiftDao.save(shift);
		if (!saved) {
			session.setAttribute("errorMessage", "保存に失敗しました。");
			redirectToEdit(res, req, monthParam, id);
			return;
		}

		// ---------- 8. 成功メッセージをセットしてリダイレクト（二重送信防止） ----------
		session.setAttribute("message", "保存しました");
		String month = (monthParam != null && !monthParam.isBlank())
				? monthParam
				: YearMonth.from(workDate).toString();
		res.sendRedirect(req.getContextPath() + "/baitolog/student/ShiftEdit.action?month=" + month);
	}

	private void redirectToEdit(HttpServletResponse res, HttpServletRequest req, String monthParam, int id) throws Exception {
		String month = (monthParam != null && !monthParam.isBlank())
				? monthParam
				: YearMonth.now().toString();
		String url = req.getContextPath() + "/baitolog/student/ShiftEdit.action?month=" + month;
		if (id > 0) {
			url += "&id=" + id;
		}
		res.sendRedirect(url);
	}

	private double round2(double v) {
		return Math.round(v * 100.0) / 100.0;
	}
}