package logic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import bean.JudgeSummary;
import bean.Shift;
import bean.WindowResult;
import bean.Workplace;

public class SevenDayJudge {


	public WindowResult judgeWindow(List<Shift> shifts, List<Workplace> workplaces,
			LocalDate from, LocalDate to, LimitPolicy policy) {
		double total = 0;

		if (shifts != null) {
			for (Shift shift : shifts) {
				LocalDate workDate = shift.getWorkDate();
				// 期間内のシフトだけを足す
				if (!workDate.isBefore(from) && !workDate.isAfter(to)) {
					total += shift.getWorkHours();
				}
			}
		}

		WindowResult result = new WindowResult();
		result.setStartDate(from);
		result.setEndDate(to);
		// 15分単位の端数が揃うよう、小数第2位で丸める
		result.setTotalHours(Math.round(total * 100) / 100.0);
		result.setLimitHours(policy.getLimitHours(from, to));
		return result;
	}

	/**
	 * judgeMonthメソッド 月の全ての7日間を判定する（教職員の学生一覧・学生別記録用）
	 *
	 * 7日間は「月の各日で終わる7日間」（月初で終わる7日間は、前月の6日分を含む）。
	 * どの7日間もちょうど1つの月に入るので、月をまたぐ7日間が前月と翌月の両方に出ることはない。
	 * shifts は「月初の6日前 ～ 月末」まで渡すこと。
	 * 一番重い7日間（超過 > 間近 > 上限内の順。同じ状態なら時間が多いほう、同じなら早いほう）を maxWindow にする
	 *
	 * @param shifts:List<Shift> 学生のシフト
	 * @param workplaces:List<Workplace> 学生の勤務先
	 * @param monthStart:LocalDate 月の1日
	 * @param monthEnd:LocalDate 月の末日
	 * @param policy:LimitPolicy 上限の決まり（長期休暇を含む）
	 * @return 判定結果:JudgeSummary
	 */
	public JudgeSummary judgeMonth(List<Shift> shifts, List<Workplace> workplaces,
			LocalDate monthStart, LocalDate monthEnd, LimitPolicy policy) {
		List<WindowResult> windows = new ArrayList<>();
		WindowResult max = null;

		for (LocalDate to = monthStart; !to.isAfter(monthEnd); to = to.plusDays(1)) {
			WindowResult window = judgeWindow(shifts, workplaces, to.minusDays(6), to, policy);
			windows.add(window);
			if (max == null || isHeavier(window, max)) {
				max = window;
			}
		}

		JudgeSummary summary = new JudgeSummary();
		summary.setWindows(windows);
		summary.setMaxWindow(max);
		summary.setStatus(max == null ? WindowResult.STATUS_OK : max.getStatus());
		return summary;
	}

	/**
	 * 状態の重さ（超過 > 間近 > 上限内）、同じなら時間の多さで比べる（同じなら先の7日間を残す）
	 */
	private boolean isHeavier(WindowResult a, WindowResult b) {
		int ra = rank(a.getStatus());
		int rb = rank(b.getStatus());
		if (ra != rb) {
			return ra > rb;
		}
		return a.getTotalHours() > b.getTotalHours();
	}

	private int rank(String status) {
		if (WindowResult.STATUS_OVER.equals(status)) {
			return 2;
		}
		return WindowResult.STATUS_CAUTION.equals(status) ? 1 : 0;
	}
}