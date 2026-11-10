package baitolog.teacher;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

import bean.School;
import bean.Student;
import bean.Teacher;
import bean.User;
import dao.StudentDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tool.Action;

/**
 * 学生のパスワードリセット（教職員）
 * 学生がパスワードを忘れたとき、パスワードを生年月日（yyyyMMdd）に戻す。
 * 戻すのはパスワードだけで、勤務先・シフトテンプレート・シフトなどは消えない。
 * 学生は次のログインで、また新しいパスワードを登録する
 *
 * 流れ（どの段階も確認してから進む）
 *   1. 確認   GET  ?id=（学生のユーザーID）   「この学生のパスワードを戻しますか？」
 *   2. 実行   POST id=・token=               戻して、学生一覧へ移動（GET StudentList.action）
 */
public class PasswordResetAction extends Action {

	/** セッションに置く名前（確認画面から実行まで、同じ学生・1回だけ使えるようにする） */
	private static final String S_TOKEN = "resetToken";
	private static final String S_ID = "resetStudentId";

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		//ローカル変数の宣言 1
		String root = req.getContextPath() + "/baitolog";
		String errorMessage = "";
		Student student = null;
		StudentDao studentDao = new StudentDao();

		//リクエストパラメータ―の取得 2
		HttpSession session = req.getSession(false);
		User user = session == null ? null : (User) session.getAttribute("user");
		if (user == null || !user.isAuthenticated() || !user.isTeacher() || !(user instanceof Teacher)) {
			// 教職員としてログインしていない場合は、ログイン画面へ戻す
			res.sendRedirect(root + "/Login.action");
			return;
		}
		School school = ((Teacher) user).getSchool();
		boolean isPost = "POST".equals(req.getMethod());
		int id = parseInt(req.getParameter("id"));
		String token = isPost ? req.getParameter("token") : null;

		//DBからデータ取得 3
		// 自分の学校の学生だけ取れる（ほかの学校の学生・存在しないIDはnull）
		if (id > 0) {
			student = studentDao.get(id, school);
		}

		//ビジネスロジック 4
		if (student == null) {
			session.setAttribute(StudentListAction.S_RESET_MESSAGE, "学生が見つかりません");
			res.sendRedirect(root + "/teacher/StudentList.action");
			return;
		}
		boolean canReset = student.getBirthday() != null;
		if (!canReset) {
			errorMessage = "この学生は生年月日が登録されていないため、リセットできません";
		}

		if (isPost) {
			Object savedToken = session.getAttribute(S_TOKEN);
			Object savedId = session.getAttribute(S_ID);
			if (savedToken == null || token == null || !savedToken.equals(token) || !Integer.valueOf(id).equals(savedId)) {
				// 期限切れ・別の学生・二重送信
				errorMessage = "確認からやり直してください";
				isPost = false;
			} else {
				// 同じ確認では二度実行できないよう、先にトークンを外す
				session.removeAttribute(S_TOKEN);
				session.removeAttribute(S_ID);

				//DBへデータ保存 5
				if (canReset && studentDao.resetPassword(student)) {
					String password = student.getBirthday().format(DateTimeFormatter.BASIC_ISO_DATE);
					session.setAttribute(StudentListAction.S_RESET_MESSAGE, student.getName() + "（" + student.getLoginId()
							+ "）のパスワードを生年月日に戻しました。初期パスワード：" + password);
					res.sendRedirect(root + "/teacher/StudentList.action");
					return;
				}
				errorMessage = "パスワードを戻せませんでした";
				isPost = false;
			}
		}

		//レスポンス値をセット 6
		// 確認画面を開くたびに、新しいトークンを作る
		String newToken = UUID.randomUUID().toString();
		session.setAttribute(S_TOKEN, newToken);
		session.setAttribute(S_ID, student.getId());
		req.setAttribute("student", student);
		req.setAttribute("canReset", canReset);
		req.setAttribute("token", newToken);
		req.setAttribute("errorMessage", errorMessage);

		//JSPへフォワード 7
		req.getRequestDispatcher("password_reset.jsp").forward(req, res);
	}

	private int parseInt(String s) {
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			return 0;
		}
	}
}
