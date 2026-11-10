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
	 * 学生番号:String
	 */
	private String no;
	
	/**
	 * 氏名:String
	 */
	private String name;
	
	/**
	 * 生年月日:String
	 */
	private String birthday;

	/**
	 * ログイン済みチェック:boolean
	 */
	private boolean firstLoginDone;
	
	/** 
	 * 学校コード:String
	 */
	private String schoolCd;
	
	/**
	 * クラス名:String
	 */
	private String className;

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

	public String getNo() { 
		return no; 
	}
	
	public void setNo(String no) { 
		this.no = no; 
	}
	
	public String getName() { 
		return name; 
	}
	
	public void setName(String name) { 
		this.name = name; 
	}
	
	public String getBirthday() { 
		return birthday; 
	}
	
	public void setBirthday(String birthday) { 
		this.birthday = birthday; 
	}
	
	public boolean isFirstLoginDone() { 
		return firstLoginDone; 
	}
	
	public void setFirstLoginDone(boolean firstLoginDone) { 
		this.firstLoginDone = firstLoginDone; 
	}
	
	public String getSchoolCd() { 
		return schoolCd; 
	}
	
	public void setSchoolCd(String schoolCd) {
		this.schoolCd = schoolCd;
	}
	
	public String getClassName() {
		return className;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}
	
}
