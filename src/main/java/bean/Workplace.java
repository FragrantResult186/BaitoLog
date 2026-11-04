package bean;

/**
 * 勤務先のBean（テーブル WORKPLACE の1行分）
 */
public class Workplace {
	/** 勤務先ID:int（新規の場合は 0） */
	private int id;
	/** 学生のユーザーID:String */
	private int studentId;
	/** 勤務先名:String */
	private String name;
	/** 店舗名:String（なければ空） */
	private String branchName;
	/** 表示色:String（例 #E53935） */
	private String color;

	/** 一覧に表示する名前（勤務先名 + 店舗名） */
	public String getDisplayName() {
		if (branchName == null || branchName.isEmpty()) {
			return name;
		}
		return name + " " + branchName;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public int getStudentId() { return studentId; }
	public void setStudentId(int studentId) { this.studentId = studentId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getBranchName() { return branchName; }
	public void setBranchName(String branchName) { this.branchName = branchName; }
	public String getColor() { return color; }
	public void setColor(String color) { this.color = color; }
}
