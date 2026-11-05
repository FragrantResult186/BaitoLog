package bean;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 報告書明細
 */
public class ReportItem implements Serializable {

	/**
	 * ID:int
	 */
	private int id;

	/**
	 * 勤務日:LocalDate
	 */
	private LocalDate workDate;

	/**
	 * 勤務先名（店舗名つき）:String
	 */
	private String workplaceName;

	/**
	 * 開始時刻:LocalTime
	 */
	private LocalTime startTime;

	/**
	 * 終了時刻:LocalTime
	 */
	private LocalTime endTime;

	/**
	 * 休憩開始時刻:LocalTime
	 */
	private LocalTime breakStart;

	/**
	 * 休憩終了時刻:LocalTime
	 */
	private LocalTime breakEnd;

	/**
	 * 労働時間:double
	 */
	private double workingHours;

	/**
	 * ゲッター、セッター
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getWorkDate() {
		return workDate;
	}

	public void setWorkDate(LocalDate workDate) {
		this.workDate = workDate;
	}

	public String getWorkplaceName() {
		return workplaceName;
	}

	public void setWorkplaceName(String workplaceName) {
		this.workplaceName = workplaceName;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public LocalTime getBreakStart() {
		return breakStart;
	}

	public void setBreakStart(LocalTime breakStart) {
		this.breakStart = breakStart;
	}

	public LocalTime getBreakEnd() {
		return breakEnd;
	}

	public void setBreakEnd(LocalTime breakEnd) {
		this.breakEnd = breakEnd;
	}

	public double getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(double workingHours) {
		this.workingHours = workingHours;
	}
}
