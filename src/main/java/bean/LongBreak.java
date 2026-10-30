package bean;

import java.io.Serializable;
import java.time.LocalDate;


public class LongBreak implements Serializable {


	private int id;

	private School school;

	private LocalDate startDate;


	private LocalDate endDate;

	private double limitHours;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = school;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public double getLimitHours() {
		return limitHours;
	}

	public void setLimitHours(double limitHours) {
		this.limitHours = limitHours;
	}
}