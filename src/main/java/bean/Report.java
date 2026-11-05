package bean;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 報告書（提出時点の内容）
 */
public class Report implements Serializable {

	/**
	 * 状態：未提出
	 */
	public static final String STATUS_UNSUBMITTED = "未提出";

	/**
	 * 状態：提出済
	 */
	public static final String STATUS_SUBMITTED = "提出済";

	/**
	 * ID:int
	 */
	private int id;

	/**
	 * 学生:Student
	 */
	private Student student;

	/**
	 * 対象年月（月初日）:LocalDate
	 */
	private LocalDate targetMonth;

	/**
	 * 未提出／提出済:String
	 */
	private String status;

	/**
	 * 提出日時:LocalDateTime
	 */
	private LocalDateTime submittedAt;

	/**
	 * 月合計:double
	 */
	private double totalHours;

	/**
	 * 7日間の最大:double
	 */
	private double max7dayHours;

	/**
	 * その開始日:LocalDate
	 */
	private LocalDate max7dayStart;

	/**
	 * 勤務先数:int
	 */
	private int workplaceCount;

	/**
	 * 明細:List<ReportItem>
	 */
	private List<ReportItem> items;

	/**
	 * ゲッター、セッター
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public LocalDate getTargetMonth() {
		return targetMonth;
	}

	public void setTargetMonth(LocalDate targetMonth) {
		this.targetMonth = targetMonth;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getSubmittedAt() {
		return submittedAt;
	}

	public void setSubmittedAt(LocalDateTime submittedAt) {
		this.submittedAt = submittedAt;
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

	public int getWorkplaceCount() {
		return workplaceCount;
	}

	public void setWorkplaceCount(int workplaceCount) {
		this.workplaceCount = workplaceCount;
	}

	public List<ReportItem> getItems() {
		return items;
	}

	public void setItems(List<ReportItem> items) {
		this.items = items;
	}

	/**
	 * 提出済みかどうか
	 */
	public boolean isSubmitted() {
		return STATUS_SUBMITTED.equals(status);
	}
}
