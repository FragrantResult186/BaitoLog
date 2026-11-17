package bean;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;


public class Shift implements Serializable {

	
	private int id;

	private int studentId;

	private int workplaceId;

	private LocalDate workDate;

	private LocalTime startTime;

	private LocalTime endTime;

	private LocalTime breakStart;

	private LocalTime breakEnd;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getStudentId() {
		return studentId;
	}

	public void setStudentId(int studentId) {
		this.studentId = studentId;
	}

	public int getWorkplaceId() {
		return workplaceId;
	}

	public void setWorkplaceId(int workplaceId) {
		this.workplaceId = workplaceId;
	}

	public LocalDate getWorkDate() {
		return workDate;
	}

	public void setWorkDate(LocalDate workDate) {
		this.workDate = workDate;
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

	public boolean hasBreak() {
		return breakStart != null && breakEnd != null;
	}

	public double getWorkHours() {
		long minutes = minutesBetween(startTime, endTime);
		if (hasBreak()) {
			// 勤務時間と重なっている部分だけを差し引く（勤務時間外の休憩は数えない）
			long workMinutes = minutes;
			long bs = minutesBetween(startTime, breakStart);
			long be = bs + minutesBetween(breakStart, breakEnd);
			long overlap = overlap(bs, be, workMinutes)
					// 勤務開始より前から始まる休憩（例：勤務9:00〜、休憩8:00〜10:00）
					+ overlap(bs - 24 * 60, be - 24 * 60, workMinutes);
			minutes -= overlap;
		}
		return Math.max(0, minutes) / 60.0;
	}

	/**
	 * 休憩が勤務開始から終了までの範囲内にあるか（休憩なしの場合は true）。
	 * 日付をまたぐ勤務にも対応する。
	 */
	public boolean isBreakWithinWork() {
		if (!hasBreak()) {
			return true;
		}
		long workMinutes = minutesBetween(startTime, endTime);
		long bs = minutesBetween(startTime, breakStart);
		long be = minutesBetween(startTime, breakEnd);
		return bs < be && be <= workMinutes;
	}

	/** 区間 [from, to) と [0, workMinutes) の重なり（分） */
	private static long overlap(long from, long to, long workMinutes) {
		return Math.max(0, Math.min(to, workMinutes) - Math.max(from, 0));
	}

	private long minutesBetween(LocalTime from, LocalTime to) {
		long minutes = Duration.between(from, to).toMinutes();
		if (minutes < 0) {
			minutes += 24 * 60;
		}
		return minutes;
	}
}