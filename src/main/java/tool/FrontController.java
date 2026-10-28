package tool;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * フロントコントローラ
 * 「〇〇.action」のURLを受け取り、対応するアクションクラスを実行する
 * 例：/baitolog/student/ShiftEdit.action → baitolog.student.ShiftEditAction
 */
@WebServlet(urlPatterns = { "*.action" })
public class FrontController extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		try {
			// パスを取得
			String path = req.getServletPath().substring(1);
			// ファイル名を取得しクラス名に変換
			String name = path.replace(".a", "A").replace('/', '.');
			// アクションクラスのインスタンスを返却
			Action action = (Action) Class.forName(name).getDeclaredConstructor().newInstance();

			// アクションクラスの処理を実行
			action.execute(req, res);

		} catch (Exception e) {
			e.printStackTrace();
			// エラーページへフォワード
			req.getRequestDispatcher("/error.jsp").forward(req, res);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		doGet(req, res);

	}
}
