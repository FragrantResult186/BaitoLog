package baitolog.student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bean.JudgeSummary;
import bean.LongBreak;
import bean.Report;
import bean.ReportItem;
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
import logic.TargetMonth;
import tool.Action;

/**
 * 報告書の提出確認（学生）
 * GET  : その月の内容を見せる（まだ保存しない）
 * POST : 「提出する」を押したとき。報告書を保存してホームへ戻る
 */
public class ReportConfirmAction extends Action {

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		// 1. ローカル変数の宣言
		HttpSession session = req.getSession();
		String root = req.getContextPath() + "/baitolog";
		ShiftDao shiftDao = new ShiftDao();
		WorkplaceDao workplaceDao = new WorkplaceDao();
		LongBreakDao longBreakDao = new LongBreakDao();
		ReportDao reportDao = new ReportDao();
		SevenDayJudge judge = new SevenDayJudge();
		LocalDate today = LocalDate.now();

		User user = (User) session.getAttribute("user");
		if (!(user instanceof Student) || !user.isAuthenticated()) {
			res.sendRedirect(root + "/Login.action");
			return;
		}
		Student student = (Student) user;

		// 2. リクエストパラメータの取得
		YearMonth targetMonth = TargetMonth.parse(req.getParameter("month"), today);
		LocalDate monthStart = targetMonth.atDay(1);
		LocalDate monthEnd = targetMonth.atEndOfMonth();
		boolean isPost = "POST".equalsIgnoreCase(req.getMethod());

		// 3. DBからデータ取得
		// すでに提出済みなら、提出した内容の画面へ
		Report existing = reportDao.get(student, monthStart);
		if (existing != null && existing.isSubmitted()) {
			res.sendRedirect(root + "/student/Report.action?month=" + targetMonth);
			return;
		}

		List<Workplace> workplaces = workplaceDao.findByStudent(student.getId());
		// 月初の6日前から取る（月をまたぐ7日間の判定に必要）
		LocalDate searchStart = monthStart.minusDays(6);
		List<Shift> shifts = shiftDao.filter(student, searchStart, monthEnd);
		List<LongBreak> longBreaks = longBreakDao.filter(student.getSchool(), searchStart, monthEnd);

		// 4. ビジネスロジック
		Map<Integer, String> workplaceNames = new HashMap<>();
		for (Workplace workplace : workplaces) {
			workplaceNames.put(workplace.getId(), workplace.getDisplayName());
		}

		// 月内のシフトを明細にする
		List<ReportItem> items = new ArrayList<>();
		Map<String, Double> workplaceHours = new LinkedHashMap<>();
		Set<String> names = new HashSet<>();
		double total = 0;
		for (Shift shift : shifts) {
			LocalDate date = shift.getWorkDate();
			if (date.isBefore(monthStart) || date.isAfter(monthEnd)) {
				continue;
			}
			String name = workplaceNames.getOrDefault(shift.getWorkplaceId(), "勤務先不明");
			double hours = shift.getWorkHours();

			ReportItem item = new ReportItem();
			item.setWorkDate(date);
			item.setWorkplaceName(name);
			item.setStartTime(shift.getStartTime());
			item.setEndTime(shift.getEndTime());
			item.setBreakStart(shift.getBreakStart());
			item.setBreakEnd(shift.getBreakEnd());
			item.setWorkingHours(hours);
			items.add(item);

			names.add(name);
			total += hours;
			workplaceHours.merge(name, hours, Double::sum);
		}
		total = Math.round(total * 100) / 100.0;

		// 月の7日間判定（一番重い7日間）
		LimitPolicy policy = new LimitPolicy(student.getSchool(), longBreaks);
		JudgeSummary summary = judge.judgeMonth(shifts, workplaces, monthStart, monthEnd, policy);
		WindowResult maxWindow = summary.getMaxWindow();

		// 報告書（まだ保存しない）
		Report report = new Report();
		report.setStudent(student);
		report.setTargetMonth(monthStart);
		report.setStatus(Report.STATUS_SUBMITTED);
		report.setSubmittedAt(LocalDateTime.now());
		report.setTotalHours(total);
		report.setMax7dayHours(maxWindow == null ? 0 : maxWindow.getTotalHours());
		report.setMax7dayStart(maxWindow == null ? null : maxWindow.getStartDate());
		report.setWorkplaceCount(names.size());
		report.setItems(items);

		String monthLabel = targetMonth.getYear() + "年" + targetMonth.getMonthValue() + "月";

		// 5. DBへデータ保存（「提出する」を押したときだけ）
		if (isPost) {
			if (items.isEmpty()) {
				req.setAttribute("errorMessage", "この月の勤務がないため、提出できません。");
			} else if (reportDao.save(report)) {
				session.setAttribute("message", monthLabel + "分の報告書を提出しました");
				res.sendRedirect(root + "/student/StudentTop.action");
				return;
			} else {
				// 同時に別の画面から提出されていた場合
				res.sendRedirect(root + "/student/Report.action?month=" + targetMonth);
				return;
			}
		}

		// 6. レスポンス値をセット
		req.setAttribute("report", report);
		req.setAttribute("maxWindow", maxWindow);
		req.setAttribute("workplaceHours", workplaceHours);
		req.setAttribute("month", targetMonth.toString());
		req.setAttribute("monthLabel", monthLabel);
		req.setAttribute("prevMonth", targetMonth.minusMonths(1).toString());
		req.setAttribute("nextMonth", targetMonth.plusMonths(1).toString());

		// 7. JSPへフォワード
		req.getRequestDispatcher("report_confirm.jsp").forward(req, res);
	}
}
