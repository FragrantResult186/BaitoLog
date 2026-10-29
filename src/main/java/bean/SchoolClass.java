package bean;

import java.io.Serializable;

/**
 * クラス
 */
public class SchoolClass implements Serializable {

	/**
	 * ID:int
	 */
	private int id;

	/**
	 * クラス名:String
	 */
	private String name;

	/**
	 * 学校:School
	 */
	private School school;

	/**
	 * ゲッター、セッター
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public School getSchool() {
		return school;
	}

	public void setSchool(School school) {
		this.school = school;
	}
}
