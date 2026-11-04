package baitolog;

import bean.User;
import dao.UserDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tool.Action;

/**
 * 新パスワード登録画面のアクション（初回ログインのときだけ使う）
 * GET：新パスワード登録画面を表示する　POST：2つの入力が一致すれば登録して、トップへ移動する
 */
public class SetPasswordAction extends Action {

	/** パスワードの最大文字数（英字・数字などの種類は問わない） */
	private static final int MAX_LENGTH = 40;

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		//ローカル変数の宣言 1
		String root = req.getContextPath() + "/baitolog";
		String newPassword = "";
		String newPassword2 = "";
		String errorMessage = "";
		boolean isFirstLogin = false;
		UserDao userDao = new UserDao();

		//リクエストパラメータ―の取得 2
		HttpSession session = req.getSession(false);
		User user = null;
		if (session != null) {
			user = (User) session.getAttribute("user");
		}
		boolean isPost = "POST".equals(req.getMethod());
		if (isPost) {
			newPassword = req.getParameter("new_password");
			newPassword2 = req.getParameter("new_password2");
			if (newPassword == null) {
				newPassword = "";
			}
			if (newPassword2 == null) {
				newPassword2 = "";
			}
		}

		//DBからデータ取得 3
		//なし

		//ビジネスロジック 4
		if (user != null) {
			isFirstLogin = !user.isPasswordInitialized();
		}
		if (!isFirstLogin) {
			// ログインしていない、または初回ログインではない場合は、ログイン画面へ戻す
			res.sendRedirect(root + "/Login.action");
			return;
		}
		boolean isOk = false;
		if (isPost) {
			if (newPassword.isEmpty() || newPassword2.isEmpty()) {
				errorMessage = "新しいパスワードを入力してください";
			} else if (newPassword.length() > MAX_LENGTH || newPassword2.length() > MAX_LENGTH) {
				errorMessage = "新しいパスワードは" + MAX_LENGTH + "文字以内で入力してください";
			} else if (!newPassword.equals(newPassword2)) {
				errorMessage = "新しいパスワードが一致しません";
			} else {
				isOk = true;
			}
		}

		//DBへデータ保存 5
		if (isOk) {
			userDao.updatePassword(user, newPassword);
		}

		//レスポンス値をセット 6
		if (isOk) {
			// 初回ログインを完了し、認証済みにする
			user.setPasswordInitialized(true);
			user.setAuthenticated(true);
		} else {
			req.setAttribute("errorMessage", errorMessage);
		}

		//JSPへフォワード 7
		if (isOk) {
			if (user.isStudent()) {
				res.sendRedirect(root + "/student/StudentTop.action");
			} else {
				res.sendRedirect(root + "/teacher/TeacherTop.action");
			}
		} else {
			req.getRequestDispatcher("set_password.jsp").forward(req, res);
		}
	}
}
