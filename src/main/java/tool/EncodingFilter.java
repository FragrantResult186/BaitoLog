package tool;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

/**
 * 文字化けを防ぐフィルタ（リクエスト・レスポンスをUTF-8にする）
 */
@WebFilter(urlPatterns = { "/*" })
public class EncodingFilter implements Filter {
	/**
	 * doFilterメソッド フィルター処理を記述
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// 文字コードをセット
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		chain.doFilter(request, response);
	}

	public void init(FilterConfig filterConfig) {
	}

	public void destroy() {
	}
}
