package bean;

import java.io.Serializable;

/**
 * 学生一覧の1行分（学生と、月の7日間判定のまとめ）
 */
public class StudentSummary implements Serializable {

	/** 判定できない（その月にシフトの登録がない） */
	public static final String STATUS_NONE = "NONE";

	private Student student;

	/** 登録している勤務先の数 */
	private int workplaceCount;

	/** その月にシフトの登録があるか（false なら「未入力」） */
	private boolean entered;

	/** 一番重い7日間（未入力なら null） */
	private WindowResult maxWindow;

	public Student getStudent() {
		return student;
	}

	public void setStudent(Student student) {
		this.student = student;
	}

	public int getWorkplaceCount() {
		return workplaceCount;
	}

	public void setWorkplaceCount(int workplaceCount) {
		this.workplaceCount = workplaceCount;
	}

	public boolean isEntered() {
		return entered;
	}

	public void setEntered(boolean entered) {
		this.entered = entered;
	}

	public WindowResult getMaxWindow() {
		return maxWindow;
	}

	public void setMaxWindow(WindowResult maxWindow) {
		this.maxWindow = maxWindow;
	}

	/**
	 * 状態（OVER／CAUTION／OK、未入力は NONE）
	 */
	public String getStatus() {
		if (!entered || maxWindow == null) {
			return STATUS_NONE;
		}
		return maxWindow.getStatus();
	}
}
