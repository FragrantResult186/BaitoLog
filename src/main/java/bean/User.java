package bean;

/**
 * ログインユーザーの親クラス（学生・教職員のBeanはこれを継承する）
 */
public class User {
	/**
	 * 認証済みフラグ:boolean true:認証済み
	 */
	private boolean isAuthenticated;

	/**
	 * ゲッター、セッター
	 */
	public boolean isAuthenticated() {
		return isAuthenticated;
	}

	public void setAuthenticated(boolean isAuthenticated) {
		this.isAuthenticated = isAuthenticated;
	}
}
