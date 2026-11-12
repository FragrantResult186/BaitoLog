package bean;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 1日分の勤務記録（学生別記録の日別グラフ・日別勤務記録用）
 */
public class DayRecord implements Serializable {

	private LocalDate date;

	/** その日のシフト（勤務がない日は空） */
	private List<Shift> shifts = new ArrayList<>();

	public DayRecord(LocalDate date) {
		this.date = date;
	}

	public LocalDate getDate() {
		return date;
	}

	public List<Shift> getShifts() {
		return shifts;
	}

	/** その日の労働時間の合計 */
	public double getHours() {
		double total = 0;
		for (Shift shift : shifts) {
			total += shift.getWorkHours();
		}
		return Math.round(total * 100) / 100.0;
	}
}
