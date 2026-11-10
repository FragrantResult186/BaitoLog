package bean;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 学生
 */
public class Student extends User implements Serializable {

	/**
	 * 所属学校:School
	 */
	private School school;

	/**
	 * 今のクラス:SchoolClass
	 */
	private SchoolClass schoolClass;

	/**
	 * 生年月日（初期パスワードに使う。登録されていなければnull）:LocalDate
	 */
	private LocalDate birthday;

	/**
	 * ゲッター、セッター
	 */
	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}

	public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = school;
	}

	public SchoolClass getSchoolClass() {
		return schoolClass;
	}

	public void setSchoolClass(SchoolClass schoolClass) {
		this.schoolClass = schoolClass;
	}
}
