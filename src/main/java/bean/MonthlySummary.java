package bean;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 月次記録の集計結果を保持する
 */
public class MonthlySummary {

    /** 対象月の1日 */
    private LocalDate month;

    /** 対象月内のシフト一覧 */
    private List<Shift> shifts;

    /** 月間合計勤務時間（時間） */
    private double totalHours;

    /** 対象期間中で最大の7日間合計勤務時間 */
    private double max7dayHours;

    /** 最大7日間の開始日 */
    private LocalDate max7dayStart;

    /** 最大7日間の終了日 */
    private LocalDate max7dayEnd;

    /** 勤務先ごとの合計勤務時間（表示名 → 時間） */
    private Map<String, Double> workplaceHours;

    public LocalDate getMonth() {
        return month;
    }

    public void setMonth(LocalDate month) {
        this.month = month;
    }

    public List<Shift> getShifts() {
        return shifts;
    }

    public void setShifts(List<Shift> shifts) {
        this.shifts = shifts;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public double getMax7dayHours() {
        return max7dayHours;
    }

    public void setMax7dayHours(double max7dayHours) {
        this.max7dayHours = max7dayHours;
    }

    public LocalDate getMax7dayStart() {
        return max7dayStart;
    }

    public void setMax7dayStart(LocalDate max7dayStart) {
        this.max7dayStart = max7dayStart;
    }

    public LocalDate getMax7dayEnd() {
        return max7dayEnd;
    }

    public void setMax7dayEnd(LocalDate max7dayEnd) {
        this.max7dayEnd = max7dayEnd;
    }

    public Map<String, Double> getWorkplaceHours() {
        return workplaceHours;
    }

    public void setWorkplaceHours(Map<String, Double> workplaceHours) {
        this.workplaceHours = workplaceHours;
    }
}