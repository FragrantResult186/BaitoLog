package baitolog.student;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
 * 報告書の提出実行（学生）
 * 報告書確認画面の「提出する」（POST）を受け取り、その月の報告書を保存する。
 * 画面は持たず、必ずリダイレクトで次の画面へ移る。
 * 報告書の内容は、画面から送られた値ではなく、DBのシフトからここで作り直す
 */
public class ReportSubmitExecuteAction extends Action {

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
		String monthLabel = targetMonth.getYear() + "年" + targetMonth.getMonthValue() + "月";

		// URLを直接開いた場合（POSTではない場合）は、確認画面へ
		if (!"POST".equalsIgnoreCase(req.getMethod())) {
			res.sendRedirect(root + "/student/ReportConfirm.action?month=" + targetMonth);
			return;
		}

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
		}
		total = Math.round(total * 100) / 100.0;

		// 勤務がない月は提出できない
		if (items.isEmpty()) {
			session.setAttribute("errorMessage", monthLabel + "は勤務がないため、提出できません。");
			res.sendRedirect(root + "/student/StudentTop.action");
			return;
		}

		// 月の7日間判定（一番重い7日間）
		LimitPolicy policy = new LimitPolicy(student.getSchool(), longBreaks);
		JudgeSummary summary = judge.judgeMonth(shifts, workplaces, monthStart, monthEnd, policy);
		WindowResult maxWindow = summary.getMaxWindow();

		// 報告書
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

		// 5. DBへデータ保存
		if (!reportDao.save(report)) {
			// 同時に別の画面から提出されていた場合は、提出した内容の画面へ
			res.sendRedirect(root + "/student/Report.action?month=" + targetMonth);
			return;
		}

		// 6. レスポンス値をセット
		// 提出完了のメッセージ（ホームで表示して消える）
		session.setAttribute("message", monthLabel + "分の報告書を提出しました");

		// 7. 次の画面へ（二重提出を防ぐため、フォワードではなくリダイレクト）
		res.sendRedirect(root + "/student/StudentTop.action");
	}
}
