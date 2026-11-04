package bean;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * クラス在籍（クラス替えの履歴）
 */
public class ClassEnrollment implements Serializable {

	/**
	 * ID:int
	 */
	private int id;

	/**
	 * 学生:Student
	 */
	private Student student;

	/**
	 * クラス:SchoolClass
	 */
	private SchoolClass schoolClass;

	/**
	 * 在籍開始日:LocalDate
	 */
	private LocalDate startDate;

	/**
	 * 在籍終了日（在籍中は null）:LocalDate
	 */
	private LocalDate endDate;

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

	public SchoolClass getSchoolClass() {
		return schoolClass;
	}

	public void setSchoolClass(SchoolClass schoolClass) {
		this.schoolClass = schoolClass;
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
}
