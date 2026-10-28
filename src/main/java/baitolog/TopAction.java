package baitolog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import dao.Dao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tool.Action;

/**
 * 起動確認用のアクション（Tomcat と H2 につながるかを確認する）
 * ログイン画面（LoginAction）ができたら、index.jsp の行き先をそちらに変えて、このクラスは削除する
 */
public class TopAction extends Action {

	@Override
	public void execute(HttpServletRequest req, HttpServletResponse res) throws Exception {

		//ローカル変数の宣言 1
		String dbVersion = "";
		Dao dao = new Dao();

		//リクエストパラメータ―の取得 2
		//なし

		//DBからデータ取得 3
		try (Connection connection = dao.getConnection();
				PreparedStatement statement = connection.prepareStatement("select H2VERSION()");
				ResultSet resultSet = statement.executeQuery()) {
			if (resultSet.next()) {
				dbVersion = resultSet.getString(1);
			}
		}

		//ビジネスロジック 4
		//なし

		//DBへデータ保存 5
		//なし

		//レスポンス値をセット 6
		req.setAttribute("dbVersion", dbVersion);

		//JSPへフォワード 7
		req.getRequestDispatcher("top.jsp").forward(req, res);
	}
}
