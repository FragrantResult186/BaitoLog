package bean;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 月次記録の集計結果（月次記録画面に渡す）
 */
public class MonthlySummary implements Serializable {

	/**
	 * 対象月（月初日）:LocalDate
	 */
	private LocalDate month;

	/**
	 * その月のシフト:List<Shift>
	 */
	private List<Shift> shifts;

	/**
	 * 月合計の労働時間:double
	 */
	private double totalHours;

	/**
	 * 対象月を含む7日間の最大の労働時間:double
	 */
	private double max7dayHours;

	/**
	 * その7日間の開始日:LocalDate
	 */
	private LocalDate max7dayStart;

	/**
	 * その7日間の終了日:LocalDate
	 */
	private LocalDate max7dayEnd;

	/**
	 * 勤務先ごとの労働時間（勤務先名 → 時間）:Map<String, Double>
	 */
	private Map<String, Double> workplaceHours = new LinkedHashMap<>();

	/**
	 * ゲッター、セッター
	 */
	public LocalDate getMonth() {
		return month;
	}

	public void setMonth(LocalDate month) {
		this.month = month;
	}

	public List<Shift> getShifts() {
		return shifts;
	}

	public void setShifts(List<Shift> shifts) {
		this.shifts = shifts;
	}

	public double getTotalHours() {
		return totalHours;
	}

	public void setTotalHours(double totalHours) {
		this.totalHours = totalHours;
	}

	public double getMax7dayHours() {
		return max7dayHours;
	}

	public void setMax7dayHours(double max7dayHours) {
		this.max7dayHours = max7dayHours;
	}

	public LocalDate getMax7dayStart() {
		return max7dayStart;
	}

	public void setMax7dayStart(LocalDate max7dayStart) {
		this.max7dayStart = max7dayStart;
	}

	public LocalDate getMax7dayEnd() {
		return max7dayEnd;
	}

	public void setMax7dayEnd(LocalDate max7dayEnd) {
		this.max7dayEnd = max7dayEnd;
	}

	public Map<String, Double> getWorkplaceHours() {
		return workplaceHours;
	}

	public void setWorkplaceHours(Map<String, Double> workplaceHours) {
		this.workplaceHours = workplaceHours;
	}
}