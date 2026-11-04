package bean;

import java.io.Serializable;
import java.util.List;

/**
 * 月の全ての7日間の判定結果
 */
public class JudgeSummary implements Serializable {

	/**
	 * 全ての7日間:List<WindowResult>
	 */
	private List<WindowResult> windows;

	/**
	 * 最大の7日間:WindowResult
	 */
	private WindowResult maxWindow;

	/**
	 * 判定状態:String
	 */
	private String status;

	/**
	 * ゲッター、セッター
	 */
	public List<WindowResult> getWindows() {
		return windows;
	}

	public void setWindows(List<WindowResult> windows) {
		this.windows = windows;
	}

	public WindowResult getMaxWindow() {
		return maxWindow;
	}

	public void setMaxWindow(WindowResult maxWindow) {
		this.maxWindow = maxWindow;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
