package baitolog.teacher;

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
 * 学生の削除（教職員）
 * 誤って登録された・卒業した・退学したなどの学生を、データごと削除する。
 * 削除の理由は必ず入力する（STUDENT_DELETE_LOG に残る）
 *
 * 流れ（どの段階も確認してから進む）
 *   1. 確認   GET  ?id=（学生のユーザーID）      削除する学生を見て、理由を入力する
 *   2. 実行   POST id=・token=・reason=          削除して、学生一覧へ移動（GET StudentList.action）
 */
public class StudentDeleteAction extends Action {

	/** セッションに置く名前（確認画面から実行まで、同じ学生・1回だけ使えるようにする） */
	private static final String S_TOKEN = "deleteToken";
	private static final String S_ID = "deleteStudentId";

	/** 学生一覧で1回だけ表示するメッセージ（StudentListAction と同じ名前） */
	private static final String S_FLASH = "studentResetMessage";

	/** 理由の最大文字数（STUDENT_DELETE_LOG.REASON の長さ） */
	private static final int REASON_MAX = 200;

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		//ローカル変数の宣言 1
		String root = req.getContextPath() + "/baitolog";
		String errorMessage = "";
		String reason = "";
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
		String token = null;
		if (isPost) {
			req.setCharacterEncoding("UTF-8");
			token = req.getParameter("token");
			reason = req.getParameter("reason") == null ? "" : req.getParameter("reason").replace('\u3000', ' ').trim();
		}

		//DBからデータ取得 3
		// 自分の学校の学生だけ取れる（ほかの学校の学生・存在しないIDはnull）
		if (id > 0) {
			student = studentDao.get(id, school);
		}

		//ビジネスロジック 4
		if (student == null) {
			session.setAttribute(S_FLASH, "学生が見つかりません");
			res.sendRedirect(root + "/teacher/StudentList.action");
			return;
		}

		if (isPost) {
			Object savedToken = session.getAttribute(S_TOKEN);
			Object savedId = session.getAttribute(S_ID);
			if (savedToken == null || token == null || !savedToken.equals(token) || !Integer.valueOf(id).equals(savedId)) {
				// 期限切れ・別の学生・二重送信
				errorMessage = "確認からやり直してください";
			} else if (reason.isEmpty()) {
				errorMessage = "削除の理由を入力してください";
			} else if (reason.length() > REASON_MAX) {
				errorMessage = "削除の理由は" + REASON_MAX + "文字以内で入力してください";
			} else {
				// 同じ確認では二度実行できないよう、先にトークンを外す
				session.removeAttribute(S_TOKEN);
				session.removeAttribute(S_ID);

				//DBへデータ保存 5
				if (studentDao.deleteStudent(student, reason, user.getId())) {
					session.setAttribute(S_FLASH, student.getName() + "（" + student.getLoginId() + "）を削除しました");
					res.sendRedirect(root + "/teacher/StudentList.action");
					return;
				}
				errorMessage = "削除できませんでした";
			}
		}

		//レスポンス値をセット 6
		// 確認画面を開くたびに、新しいトークンを作る
		String newToken = UUID.randomUUID().toString();
		session.setAttribute(S_TOKEN, newToken);
		session.setAttribute(S_ID, student.getId());
		req.setAttribute("student", student);
		req.setAttribute("reason", reason);
		req.setAttribute("token", newToken);
		req.setAttribute("errorMessage", errorMessage);

		//JSPへフォワード 7
		req.getRequestDispatcher("student_delete.jsp").forward(req, res);
	}

	private int parseInt(String s) {
		try {
			return Integer.parseInt(s.trim());
		} catch (Exception e) {
			return 0;
		}
	}
}
