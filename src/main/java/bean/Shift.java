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
			minutes -= minutesBetween(breakStart, breakEnd);
		}
		return Math.max(0, minutes) / 60.0;
	}

	private long minutesBetween(LocalTime from, LocalTime to) {
		long minutes = Duration.between(from, to).toMinutes();
		if (minutes < 0) {
			minutes += 24 * 60;
		}
		return minutes;
	}
}