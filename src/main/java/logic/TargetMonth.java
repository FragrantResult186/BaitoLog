package logic;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * 対象期間（月）の扱い。画面では「yyyy-MM」の文字（target_month）でやりとりする
 */
public class TargetMonth {

	/** 選べる月：今月の何か月前から */
	private static final int MONTHS_BEFORE = 12;

	/** 選べる月：今月の何か月先まで */
	private static final int MONTHS_AFTER = 1;

	/**
	 * parseメソッド 文字を月にする（空・形式違い・選べる範囲の外は、今月）
	 *
	 * @param text:String yyyy-MM
	 * @param today:LocalDate 今日
	 * @return 月:YearMonth
	 */
	public static YearMonth parse(String text, LocalDate today) {
		YearMonth current = YearMonth.from(today);
		if (text == null || text.isBlank()) {
			return current;
		}
		try {
			YearMonth month = YearMonth.parse(text.trim());
			if (month.isBefore(current.minusMonths(MONTHS_BEFORE)) || month.isAfter(current.plusMonths(MONTHS_AFTER))) {
				return current;
			}
			return month;
		} catch (Exception e) {
			return current;
		}
	}

	/**
	 * optionsメソッド 選べる月（新しい月から順）
	 *
	 * @param today:LocalDate 今日
	 * @return 月のリスト:List<YearMonth>
	 */
	public static List<YearMonth> options(LocalDate today) {
		YearMonth current = YearMonth.from(today);
		List<YearMonth> list = new ArrayList<>();
		for (int i = MONTHS_AFTER; i >= -MONTHS_BEFORE; i--) {
			list.add(current.plusMonths(i));
		}
		return list;
	}
}
