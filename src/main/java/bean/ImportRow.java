package bean;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 学生CSVの1行
 */
public class ImportRow implements Serializable {

	/**
	 * 種類：新規（まだ登録されていない学生番号）
	 */
	public static final String KIND_NEW = "NEW";

	/**
	 * 種類：更新（登録済みの学生で、氏名・クラス・生年月日のどれかが違う）
	 */
	public static final String KIND_UPDATE = "UPDATE";

	/**
	 * 種類：変更なし（登録済みの学生で、内容が同じ）
	 */
	public static final String KIND_UNCHANGED = "UNCHANGED";

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
	 * 種類（新規／更新／変更なし）:String
	 */
	private String kind;

	/**
	 * 更新する学生のユーザーID（更新・変更なしのとき）:int
	 */
	private int userId;

	/**
	 * 更新前の氏名:String
	 */
	private String oldName;

	/**
	 * 更新前のクラス名（クラスに在籍していなければ null）:String
	 */
	private String oldClassName;

	/**
	 * 更新前の生年月日（未登録なら null）:LocalDate
	 */
	private LocalDate oldBirthday;

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

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public String getOldClassName() {
		return oldClassName;
	}

	public void setOldClassName(String oldClassName) {
		this.oldClassName = oldClassName;
	}

	public LocalDate getOldBirthday() {
		return oldBirthday;
	}

	public void setOldBirthday(LocalDate oldBirthday) {
		this.oldBirthday = oldBirthday;
	}

	/**
	 * 氏名が更新前と違うか（更新の行で、確認画面の表示に使う）
	 */
	public boolean isNameChanged() {
		return !Objects.equals(oldName, name);
	}

	/**
	 * クラスが更新前と違うか
	 */
	public boolean isClassChanged() {
		return !Objects.equals(oldClassName, className);
	}

	/**
	 * 生年月日が更新前と違うか
	 */
	public boolean isBirthdayChanged() {
		return !Objects.equals(oldBirthday, birthday);
	}
}