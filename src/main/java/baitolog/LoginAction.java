package baitolog;

import bean.User;
import dao.UserDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tool.Action;

/**
 * ログイン画面のアクション
 * GET：ログイン画面を表示する　POST：認証する
 * 認証に成功したら、初回ログインなら新パスワード登録へ、2回目以降は学生／教職員のトップへ移動する
 */
public class LoginAction extends Action {

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		//ローカル変数の宣言 1
		String loginId = "";
		String password = "";
		String errorMessage = "";
		User user = null;
		boolean isFirstLogin = false;
		UserDao userDao = new UserDao();

		//リクエストパラメータ―の取得 2
		boolean isPost = "POST".equals(req.getMethod());
		if (isPost) {
			loginId = req.getParameter("username");
			password = req.getParameter("password");
			if (loginId == null) {
				loginId = "";
			}
			if (password == null) {
				password = "";
			}
		}

		//DBからデータ取得 3
		if (isPost && !loginId.isEmpty() && !password.isEmpty()) {
			// 学生・教職員とも USERS から認証する（パスワードは UserDao がハッシュ化して照合）
			user = userDao.login(loginId, password);
		}

		//ビジネスロジック 4
		if (isPost && user == null) {
			// 未入力、または ID・パスワードの不一致
			errorMessage = "IDまたはパスワードが違います";
		}
		if (user != null) {
			// 初回パスワード未登録なら新パスワード登録へ
			isFirstLogin = !user.isPasswordInitialized();
		}
		if (isFirstLogin) {
			// 新パスワードを登録するまでは「認証済み」にしない（ほかの画面を開けないようにする）
			user.setAuthenticated(false);
		}

		//DBへデータ保存 5
		//なし

		//レスポンス値をセット 6
		if (user != null) {
			// 認証成功：古いセッションを破棄して、新しいセッションにユーザーを入れる
			HttpSession oldSession = req.getSession(false);
			if (oldSession != null) {
				oldSession.invalidate();
			}
			req.getSession(true).setAttribute("user", user);
		} else {
			// 画面に戻すとき、ユーザーIDは入力したままにする（パスワードは戻さない）
			req.setAttribute("username", loginId);
			req.setAttribute("errorMessage", errorMessage);
		}

		//JSPへフォワード 7
		if (user != null) {
			String root = req.getContextPath() + "/baitolog";
			if (isFirstLogin) {
				res.sendRedirect(root + "/SetPassword.action");
			} else if (user.isStudent()) {
				res.sendRedirect(root + "/student/StudentTop.action");
			} else {
				res.sendRedirect(root + "/teacher/TeacherTop.action");
			}
		} else {
			req.getRequestDispatcher("login.jsp").forward(req, res);
		}
	}
}
