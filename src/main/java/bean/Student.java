package bean;

import java.io.Serializable;

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
	 * ゲッター、セッター
	 */
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
