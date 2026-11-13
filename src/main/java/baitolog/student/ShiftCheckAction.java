package baitolog.student;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import bean.LongBreak;
import bean.Shift;
import bean.Student;
import bean.User;
import bean.WindowResult;
import dao.LongBreakDao;
import dao.ShiftDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import logic.LimitPolicy;
import logic.SevenDayJudge;
import tool.Action;

/**
 * 入力中のシフトについて、影響する7日間を判定する
 */
public class ShiftCheckAction extends Action {

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		res.setContentType("application/json; charset=UTF-8");
		PrintWriter out = res.getWriter();

		// 1. ログイン確認
		HttpSession session = req.getSession(false);
		if (session == null) {
			out.print("{\"ok\":false,\"message\":\"ログインが必要です\"}");
			return;
		}
		User user = (User) session.getAttribute("user");
		if (!(user instanceof Student) || !user.isAuthenticated()) {
			out.print("{\"ok\":false,\"message\":\"ログインが必要です\"}");
			return;
		}
		Student student = (Student) user;

		// 2. パラメータ取得
		String dateText = req.getParameter("date");
		String startText = req.getParameter("start");
		String endText = req.getParameter("end");
		String breakStartText = req.getParameter("breakStart");
		String breakEndText = req.getParameter("breakEnd");
		String idText = req.getParameter("id"); // 編集中のシフトID（新規は空 or 0）

		if (isEmpty(dateText) || isEmpty(startText) || isEmpty(endText)) {
			out.print("{\"ok\":false,\"message\":\"日付・開始・終了を入力してください\"}");
			return;
		}

		LocalDate workDate;
		LocalTime startTime;
		LocalTime endTime;
		LocalTime breakStart = null;
		LocalTime breakEnd = null;
		int editId = 0;

		try {
			workDate = LocalDate.parse(dateText);
			startTime = LocalTime.parse(startText);
			endTime = LocalTime.parse(endText);
			if (!isEmpty(breakStartText)) {
				breakStart = LocalTime.parse(breakStartText);
			}
			if (!isEmpty(breakEndText)) {
				breakEnd = LocalTime.parse(breakEndText);
			}
			if (!isEmpty(idText)) {
				editId = Integer.parseInt(idText);
			}
		} catch (Exception e) {
			out.print("{\"ok\":false,\"message\":\"日付または時刻の形式が正しくありません\"}");
			return;
		}

		// 3. 仮シフトを作成
		Shift candidate = new Shift();
		candidate.setId(editId);
		candidate.setStudentId(student.getId());
		candidate.setWorkDate(workDate);
		candidate.setStartTime(startTime);
		candidate.setEndTime(endTime);
		candidate.setBreakStart(breakStart);
		candidate.setBreakEnd(breakEnd);

		if (candidate.getWorkHours() <= 0) {
			out.print("{\"ok\":false,\"message\":\"労働時間が0時間以下です\"}");
			return;
		}

		// 4. 前後6日分のシフトを取得（編集中のIDは除外）
		LocalDate searchFrom = workDate.minusDays(6);
		LocalDate searchTo = workDate.plusDays(6);

		ShiftDao shiftDao = new ShiftDao();
		List<Shift> shifts = new ArrayList<>();
		for (Shift s : shiftDao.filter(student, searchFrom, searchTo)) {
			if (s.getId() != editId) {
				shifts.add(s);
			}
		}
		shifts.add(candidate);

		// 5. 長期休暇と上限
		if (student.getSchool() == null) {
			out.print("{\"ok\":false,\"message\":\"学校情報が取得できません\"}");
			return;
		}
		LongBreakDao longBreakDao = new LongBreakDao();
		List<LongBreak> longBreaks = longBreakDao.filter(student.getSchool(), searchFrom, searchTo);
		LimitPolicy policy = new LimitPolicy(student.getSchool(), longBreaks);

		// 6. 選択日を含む7通りの7日間を判定し、最悪の結果を取る
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

		// 7. JSONで返す
		boolean over = worst.isOver();
		double total = worst.getTotalHours();
		double limit = worst.getLimitHours();
		double remaining = worst.getRemainingHours();
		double overHours = worst.getOverHours();

		StringBuilder json = new StringBuilder();
		json.append("{");
		json.append("\"ok\":true,");
		json.append("\"over\":").append(over).append(",");
		json.append("\"totalHours\":").append(round2(total)).append(",");
		json.append("\"limitHours\":").append(round2(limit)).append(",");
		json.append("\"remainingHours\":").append(round2(remaining)).append(",");
		json.append("\"overHours\":").append(round2(overHours)).append(",");
		json.append("\"startDate\":\"").append(worst.getStartDate()).append("\",");
		json.append("\"endDate\":\"").append(worst.getEndDate()).append("\",");
		if (over) {
			json.append("\"message\":\"上限を超えます（").append(round2(total)).append("時間）\"");
		} else {
			json.append("\"message\":\"登録しても7日間の上限内です（").append(round2(total)).append("時間）\"");
		}
		json.append("}");
		out.print(json.toString());
	}

	private boolean isEmpty(String s) {
		return s == null || s.isBlank();
	}

	private double round2(double v) {
		return Math.round(v * 100.0) / 100.0;
	}
}