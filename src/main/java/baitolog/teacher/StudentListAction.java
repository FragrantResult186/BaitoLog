package baitolog.teacher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import bean.School;
import bean.SchoolClass;
import bean.Student;
import bean.Teacher;
import bean.User;
import dao.SchoolClassDao;
import dao.StudentDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tool.Action;

/**
 * 学生一覧（教職員）
 * 自分の学校の学生を、クラス・氏名・学生番号で絞り込んで見る。
 * 学生ごとに「パスワードリセット」の画面へ進める
 *
 * GET ?classId=（クラスで絞る） &q=（氏名か学生番号の一部で絞る）
 */
public class StudentListAction extends Action {

	/** セッションに置く名前（パスワードリセットのあとに1回だけ表示する） */
	public static final String S_RESET_MESSAGE = "studentResetMessage";

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		//ローカル変数の宣言 1
		String root = req.getContextPath() + "/baitolog";
		String errorMessage = "";
		List<Student> students = new ArrayList<>();
		List<SchoolClass> classes = new ArrayList<>();
		SchoolClass selectedClass = null;
		StudentDao studentDao = new StudentDao();
		SchoolClassDao schoolClassDao = new SchoolClassDao();

		//リクエストパラメータ―の取得 2
		HttpSession session = req.getSession(false);
		User user = session == null ? null : (User) session.getAttribute("user");
		if (user == null || !user.isAuthenticated() || !user.isTeacher() || !(user instanceof Teacher)) {
			// 教職員としてログインしていない場合は、ログイン画面へ戻す
			res.sendRedirect(root + "/Login.action");
			return;
		}
		School school = ((Teacher) user).getSchool();
		String classIdText = req.getParameter("classId");
		String keyword = req.getParameter("q") == null ? "" : req.getParameter("q").trim();

		//DBからデータ取得 3
		classes = schoolClassDao.filter(school);
		LocalDate today = LocalDate.now();

		//ビジネスロジック 4
		// 絞り込むクラス（自分の学校のクラスだけ選べる）
		if (classIdText != null && !classIdText.isEmpty()) {
			for (SchoolClass schoolClass : classes) {
				if (String.valueOf(schoolClass.getId()).equals(classIdText)) {
					selectedClass = schoolClass;
				}
			}
			if (selectedClass == null) {
				errorMessage = "選んだクラスが見つかりません";
			}
		}
		if (selectedClass != null) {
			students = studentDao.filter(school, selectedClass, today);
		} else {
			students = studentDao.filter(school, today);
		}
		// 氏名か学生番号に、入力した文字が入っている学生だけ残す（空白は無視する）
		if (!keyword.isEmpty()) {
			String key = normalize(keyword);
			List<Student> matched = new ArrayList<>();
			for (Student student : students) {
				if (normalize(student.getName()).contains(key) || normalize(student.getLoginId()).contains(key)) {
					matched.add(student);
				}
			}
			students = matched;
		}

		//DBへデータ保存 5
		// 一覧は見るだけなので、保存はしない

		//レスポンス値をセット 6
		// パスワードリセットのあとに1回だけ出すメッセージ
		Object flash = session.getAttribute(S_RESET_MESSAGE);
		if (flash != null) {
			req.setAttribute("resetMessage", flash);
			session.removeAttribute(S_RESET_MESSAGE);
		}
		req.setAttribute("students", students);
		req.setAttribute("classes", classes);
		req.setAttribute("selectedClassId", selectedClass == null ? "" : String.valueOf(selectedClass.getId()));
		req.setAttribute("keyword", keyword);
		req.setAttribute("errorMessage", errorMessage);

		//JSPへフォワード 7
		req.getRequestDispatcher("student_list.jsp").forward(req, res);
	}

	/**
	 * 空白（全角も）を消して、小文字にそろえる
	 */
	private String normalize(String s) {
		return s == null ? "" : s.replace("　", "").replace(" ", "").toLowerCase();
	}
}
