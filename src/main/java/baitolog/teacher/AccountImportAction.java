package baitolog.teacher;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import bean.ImportRow;
import bean.School;
import bean.SchoolClass;
import bean.Teacher;
import bean.User;
import dao.SchoolClassDao;
import dao.StudentDao;
import dao.UserDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import logic.ImportFileReader;
import tool.Action;

/**
 * 学生アカウント一括登録（教職員）
 * 学校の学生ファイル（CSV・xlsx）を読み込み、確認しながら登録する。初期パスワードは生年月日（yyyyMMdd）
 *
 * 画面の流れ（どの段階も確認してから次へ進む）
 *   1. アップロード   GET                       ファイルを選ぶ
 *   2. 内容の確認     POST action=check         読み込んだ内容と、エラーのある行を見る
 *   3. 登録の最終確認 POST action=confirm       「N人を登録します」を見る
 *   4. 結果           POST action=register      登録して、結果の画面へ移動（GET ?done=1）
 *   戻る              POST action=preview       最終確認から内容の確認へ戻る
 *                     POST action=back          最初のアップロード画面へ戻る
 *
 * 読み込んだ行はセッションに置き、確認画面から登録まで同じ内容を使う（トークンで二重登録を防ぐ）
 */
public class AccountImportAction extends Action {

	/** セッションに置く名前 */
	private static final String S_ROWS = "importRows";
	private static final String S_TOKEN = "importToken";
	private static final String S_FILE = "importFileName";
	private static final String S_CREATED = "importCreated";
	private static final String S_SKIPPED = "importSkipped";

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		//ローカル変数の宣言 1
		String root = req.getContextPath() + "/baitolog";
		String errorMessage = "";
		String mode = "upload";
		String fileName = "";
		List<ImportRow> rows = new ArrayList<>();
		UserDao userDao = new UserDao();
		SchoolClassDao schoolClassDao = new SchoolClassDao();
		StudentDao studentDao = new StudentDao();

		//リクエストパラメータ―の取得 2
		HttpSession session = req.getSession(false);
		User user = null;
		if (session != null) {
			user = (User) session.getAttribute("user");
		}
		if (user == null || !user.isAuthenticated() || !user.isTeacher() || !(user instanceof Teacher)) {
			// 教職員としてログインしていない場合は、ログイン画面へ戻す
			res.sendRedirect(root + "/Login.action");
			return;
		}
		School school = ((Teacher) user).getSchool();
		boolean isPost = "POST".equals(req.getMethod());
		String action = isPost ? req.getParameter("action") : null;
		String token = isPost ? req.getParameter("token") : null;
		boolean isDone = !isPost && "1".equals(req.getParameter("done"));

		//DBからデータ取得 3
		// 登録するたびに最新を調べる（確認画面を開いている間に登録された学生番号を見逃さないため）
		//→ 4 のビジネスロジックの中で、必要なときに呼ぶ

		//ビジネスロジック 4
		if (isDone) {
			// 結果画面（登録後にリダイレクトされてきた）
			Object created = session.getAttribute(S_CREATED);
			if (created == null) {
				res.sendRedirect(root + "/teacher/AccountImport.action");
				return;
			}
			mode = "result";
			req.setAttribute("createdCount", created);
			req.setAttribute("skippedRows", session.getAttribute(S_SKIPPED));
			session.removeAttribute(S_CREATED);
			session.removeAttribute(S_SKIPPED);
		} else if ("check".equals(action)) {
			// 2 ファイルを読み込んで、内容を確認する画面へ
			clearImport(session);
			try {
				Part part = req.getPart("file");
				if (part == null || part.getSize() == 0) {
					throw new Exception("ファイルを選んでください");
				}
				if (part.getSize() > ImportFileReader.MAX_BYTES) {
					throw new Exception("ファイルが大きすぎます（2MBまで）");
				}
				fileName = part.getSubmittedFileName();
				byte[] data;
				try (InputStream in = part.getInputStream()) {
					data = in.readNBytes(ImportFileReader.MAX_BYTES + 1);
				}
				rows = ImportFileReader.read(data, fileName);
				checkRows(rows, school, userDao, schoolClassDao);
				// クラス、学生番号の順に並べる
				rows.sort(Comparator.comparing(ImportRow::getClassName).thenComparing(ImportRow::getStudentNo));

				token = UUID.randomUUID().toString();
				session.setAttribute(S_ROWS, rows);
				session.setAttribute(S_TOKEN, token);
				session.setAttribute(S_FILE, fileName);
				mode = "preview";
			} catch (Exception e) {
				// 形式・見出しなど、ファイル全体が読めないとき（画面に出せる文章）
				errorMessage = e.getMessage() == null ? "ファイルを読み込めませんでした" : e.getMessage();
				mode = "upload";
			}
		} else if ("preview".equals(action) || "confirm".equals(action) || "register".equals(action)) {
			rows = loadRows(session, token);
			if (rows == null) {
				// 期限切れ・トークン違い・二重送信
				errorMessage = "確認の内容が見つかりません。もう一度ファイルを選んでください";
				mode = "upload";
				rows = new ArrayList<>();
			} else {
				fileName = (String) session.getAttribute(S_FILE);
				int errorsBefore = countErrors(rows);
				// 登録直前にもう一度、DBの状態と照らし合わせる
				checkRows(rows, school, userDao, schoolClassDao);
				if (countErrors(rows) != errorsBefore) {
					errorMessage = "登録済みのデータが変わったため、もう一度内容を確認してください";
					mode = "preview";
				} else if (countErrors(rows) == rows.size()) {
					errorMessage = "登録できる学生がいません";
					mode = "preview";
				} else if ("preview".equals(action)) {
					// 最終確認から内容の確認へ戻る
					mode = "preview";
				} else if ("confirm".equals(action)) {
					mode = "confirm";
				} else {
					// 5 登録（同じ確認内容では二度登録できないよう、先にセッションから外す）
					List<ImportRow> skipped = new ArrayList<>();
					for (ImportRow row : rows) {
						if (row.getError() != null) {
							skipped.add(row);
						}
					}
					clearImport(session);
					try {
						int created = studentDao.saveAll(rows, school);
						session.setAttribute(S_CREATED, created);
						session.setAttribute(S_SKIPPED, skipped);
						mode = "done-redirect";
					} catch (Exception e) {
						e.printStackTrace();
						errorMessage = "登録に失敗しました（1人も登録されていません）。もう一度最初からやり直してください";
						mode = "upload";
						rows = new ArrayList<>();
					}
				}
			}
		} else if ("back".equals(action)) {
			clearImport(session);
		}

		//DBへデータ保存 5
		// 登録は上の「register」の中で studentDao.saveAll が行う

		//レスポンス値をセット 6
		if (!"done-redirect".equals(mode)) {
			int errorCount = countErrors(rows);
			req.setAttribute("mode", mode);
			req.setAttribute("rows", rows);
			req.setAttribute("rowCount", rows.size());
			req.setAttribute("errorCount", errorCount);
			req.setAttribute("okCount", rows.size() - errorCount);
			req.setAttribute("fileName", fileName);
			req.setAttribute("token", session.getAttribute(S_TOKEN));
			req.setAttribute("errorMessage", errorMessage);
		}

		//JSPへフォワード 7
		if ("done-redirect".equals(mode)) {
			res.sendRedirect(root + "/teacher/AccountImport.action?done=1");
		} else {
			req.getRequestDispatcher("account_import.jsp").forward(req, res);
		}
	}

	/**
	 * checkRowsメソッド DBの状態と照らし合わせて、各行の error を決める
	 * ファイルの中の問題（空欄・生年月日）は ImportFileReader が先に error に入れている
	 * （すでに error がある行は、最初の理由を残す）
	 *
	 * @param rows:List<ImportRow> 行
	 * @param school:School 教職員の学校
	 */
	private void checkRows(List<ImportRow> rows, School school, UserDao userDao, SchoolClassDao schoolClassDao)
			throws Exception {
		// 学生番号がファイルの中で重なっているか数える
		Map<String, Integer> counts = new HashMap<>();
		for (ImportRow row : rows) {
			counts.merge(row.getStudentNo(), 1, Integer::sum);
		}

		// すでに登録されている学生番号
		List<String> numbers = new ArrayList<>();
		for (ImportRow row : rows) {
			if (!row.getStudentNo().isEmpty()) {
				numbers.add(row.getStudentNo());
			}
		}
		Set<String> registered = new HashSet<>(userDao.filterLoginIds(numbers));

		// 学校にあるクラスの名前
		Set<String> classNames = new HashSet<>();
		for (SchoolClass schoolClass : schoolClassDao.filter(school)) {
			classNames.add(schoolClass.getName());
		}

		for (ImportRow row : rows) {
			if (isFileError(row)) {
				continue;
			}
			// 前回の照合の結果は消して、やり直す
			row.setError(null);
			if (counts.get(row.getStudentNo()) > 1) {
				row.setError("学生番号がファイルの中で重なっています");
			} else if (registered.contains(row.getStudentNo())) {
				row.setError("この学生番号はすでに登録されています");
			} else if (!classNames.contains(row.getClassName())) {
				row.setError("クラス「" + row.getClassName() + "」が学校に登録されていません");
			}
		}
	}

	/**
	 * ファイルの中の問題（空欄・生年月日）かどうか
	 * これらはDBの状態では変わらないので、照合のやり直しで消さない
	 */
	private boolean isFileError(ImportRow row) {
		String e = row.getError();
		return e != null && (e.endsWith("が空です") || e.startsWith("生年月日の形式"));
	}

	private int countErrors(List<ImportRow> rows) {
		int n = 0;
		for (ImportRow row : rows) {
			if (row.getError() != null) {
				n++;
			}
		}
		return n;
	}

	/**
	 * loadRowsメソッド セッションの行を取り出す（トークンが合わなければ null）
	 */
	@SuppressWarnings("unchecked")
	private List<ImportRow> loadRows(HttpSession session, String token) {
		Object saved = session.getAttribute(S_TOKEN);
		if (saved == null || token == null || !saved.equals(token)) {
			return null;
		}
		return (List<ImportRow>) session.getAttribute(S_ROWS);
	}

	private void clearImport(HttpSession session) {
		session.removeAttribute(S_ROWS);
		session.removeAttribute(S_TOKEN);
		session.removeAttribute(S_FILE);
	}
}
