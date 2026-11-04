package bean;

/**
 * シフトテンプレートのBean（テーブル SHIFT_TEMPLATE の1行分）
 */
public class ShiftTemplate {
	/** テンプレートID:int（新規の場合は 0） */
	private int id;
	/** 勤務先ID:int */
	private int workplaceId;
	/** テンプレート名:String（例 平日夕方） */
	private String name;
	/** 開始時刻:String（HH:mm） */
	private String startTime;
	/** 終了時刻:String（HH:mm） */
	private String endTime;
	/** 休憩時間（分）:int（休憩なしは 0） */
	private int breakMin;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public int getWorkplaceId() { return workplaceId; }
	public void setWorkplaceId(int workplaceId) { this.workplaceId = workplaceId; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getStartTime() { return startTime; }
	public void setStartTime(String startTime) { this.startTime = startTime; }
	public String getEndTime() { return endTime; }
	public void setEndTime(String endTime) { this.endTime = endTime; }
	public int getBreakMin() { return breakMin; }
	public void setBreakMin(int breakMin) { this.breakMin = breakMin; }
}
