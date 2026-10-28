package tool;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * すべてのアクションクラスの親クラス
 * 各アクションは execute メソッドに処理を書く（手順1〜7）
 */
public abstract class Action {

	public abstract void execute(
			HttpServletRequest req, HttpServletResponse res
		) throws Exception;

}
