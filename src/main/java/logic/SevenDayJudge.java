package logic;

import java.time.LocalDate;
import java.util.List;

import bean.Shift;
import bean.WindowResult;

public class SevenDayJudge {


	public WindowResult judgeWindow(List<Shift> shifts, LocalDate from, LocalDate to, LimitPolicy policy) {
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
}