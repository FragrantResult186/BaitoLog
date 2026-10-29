package logic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * パスワードのハッシュ化と初期パスワードの作成
 * DBにはパスワードをそのまま保存せず、hash() した値を保存する
 */
public class PasswordUtil {

	/**
	 * 初期パスワードに使う文字（見間違えやすい 0 O 1 l I は使わない）
	 */
	private static final String CHARS = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

	/**
	 * hashメソッド パスワードを SHA-256 のハッシュ値（16進64文字）にする
	 *
	 * @param password:String
	 *            パスワード
	 * @return ハッシュ値:String
	 * @throws Exception
	 */
	public static String hash(String password) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
		StringBuilder builder = new StringBuilder();
		for (byte b : bytes) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}

	/**
	 * generateメソッド 8文字の初期パスワードを作る
	 *
	 * @return 初期パスワード:String
	 */
	public static String generate() {
		SecureRandom random = new SecureRandom();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 8; i++) {
			builder.append(CHARS.charAt(random.nextInt(CHARS.length())));
		}
		return builder.toString();
	}
}
