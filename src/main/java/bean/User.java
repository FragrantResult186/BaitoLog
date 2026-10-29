package bean;

/**
 * ログインユーザーの親クラス（学生・教職員のBeanはこれを継承する）
 */
public class User {

	/**
	 * 権限：学生
	 */
	public static final String ROLE_STUDENT = "STUDENT";

	/**
	 * 権限：教職員
	 */
	public static final String ROLE_TEACHER = "TEACHER";

	/**
	 * ユーザーID（連番）:int
	 */
	private int id;

	/**
	 * ログインID＝学生番号／職員ID:String
	 */
	private String loginId;

	/**
	 * 氏名:String
	 */
	private String name;

	/**
	 * 権限 STUDENT／TEACHER:String
	 */
	private String role;

	/**
	 * 初回パスワード登録済み:boolean
	 */
	private boolean passwordInitialized;

	/**
	 * 認証済み:boolean
	 */
	private boolean isAuthenticated;

	/**
	 * ゲッター、セッター
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isPasswordInitialized() {
		return passwordInitialized;
	}

	public void setPasswordInitialized(boolean passwordInitialized) {
		this.passwordInitialized = passwordInitialized;
	}

	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	public void setAuthenticated(boolean isAuthenticated) {
		this.isAuthenticated = isAuthenticated;
	}

	/**
	 * 学生かどうか
	 */
	public boolean isStudent() {
		return ROLE_STUDENT.equals(role);
	}

	/**
	 * 教職員かどうか
	 */
	public boolean isTeacher() {
		return ROLE_TEACHER.equals(role);
	}
}
