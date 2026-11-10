package bean;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 学生CSVの1行
 */
public class ImportRow implements Serializable {

	/**
	 * CSVの行番号:int
	 */
	private int lineNo;

	/**
	 * 氏名:String
	 */
	private String name;

	/**
	 * 学生番号:String
	 */
	private String studentNo;

	/**
	 * クラス名:String
	 */
	private String className;

	/**
	 * 生年月日（初期パスワードに使う）:LocalDate
	 */
	private LocalDate birthday;

	/**
	 * 重複・空欄:String
	 */
	private String error;

	/**
	 * ゲッター、セッター
	 */
	public int getLineNo() {
		return lineNo;
	}

	public void setLineNo(int lineNo) {
		this.lineNo = lineNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStudentNo() {
		return studentNo;
	}

	public void setStudentNo(String studentNo) {
		this.studentNo = studentNo;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}
