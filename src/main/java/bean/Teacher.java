package bean;

import java.io.Serializable;

/**
 * 教職員
 */
public class Teacher extends User implements Serializable {

	/**
	 * 所属学校:School
	 */
	private School school;

	/**
	 * ゲッター、セッター
	 */
	public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = school;
	}
}
