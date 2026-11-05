package bean;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class WindowResult implements Serializable {
	
	public static final String STATUS_OK = "OK";
	
	public static final String STATUS_CAUTION = "CAUTION";
	
	public static final String STATUS_OVER = "OVER";
	
	private static final double CAUTION_RATE = 0.8;
	
	private LocalDate startDate;
	
	private LocalDate endDate;
	
	private double totalHours;
	
	private double limitHours;
	
	private Map<String, Double> workplaceHours = new LinkedHashMap<>();
	
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
	
	public double getTotalHours() {
		return totalHours;
	}
	
	public void setTotalHours(double totalHours) {
		this.totalHours = totalHours;
	}
 
	public double getLimitHours() {
		return limitHours;
	}
 
	public void setLimitHours(double limitHours) {
		this.limitHours = limitHours;
	}
 
	public Map<String, Double> getWorkplaceHours() {
		return workplaceHours;
	}
	
	public void setWorkplaceHours(Map<String, Double> workplaceHours) {
		this.workplaceHours = workplaceHours;
	}
	
	public void addWorkplaceHours(String workplaceName, double hours) {
		workplaceHours.merge(workplaceName, hours, Double::sum);
		totalHours += hours;
	}
	
	public double getRemainingHours() {
		return Math.max(0, limitHours - totalHours);
	}
	
	public double getOverHours() {
		return Math.max(0, totalHours - limitHours);
	}
 
	public int getPercent() {
		if (limitHours <= 0) {
			return 0;
		}
		return (int) (totalHours / limitHours * 100);
	}
 
	public boolean isOver() {
		return totalHours > limitHours;
	}
 
	public String getStatus() {
		if (isOver()) {
			return STATUS_OVER;
		}
		if (totalHours >= limitHours * CAUTION_RATE) {
			return STATUS_CAUTION;
		}
		return STATUS_OK;
	}
	
}
