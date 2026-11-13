package baitolog.student;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import bean.LongBreak;
import bean.Shift;
import bean.Student;
import bean.WindowResult;
import bean.Workplace;
import dao.LongBreakDao;
import dao.ShiftDao;
import dao.WorkplaceDao;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import logic.LimitPolicy;
import tool.Action;

/**
 * 仮シフトを追加した場合の7日間労働時間を判定するAction
 *
 * URL:
 * /baitolog/student/SimulateExecute.action
 *
 * このクラスでは仮シフトをDBへ保存しない。
 */
public class SimulateExecuteAction extends Action {

    @Override
    public void execute(
            HttpServletRequest req,
            HttpServletResponse res) throws Exception {

        // --------------------------------------------------
        // 1. ログイン確認
        // --------------------------------------------------

        HttpSession session = req.getSession(false);

        if (session == null) {
            res.sendRedirect(
                req.getContextPath() + "/baitolog/Login.action"
            );
            return;
        }

        Object loginUser = session.getAttribute("user");

        if (!(loginUser instanceof Student)) {
            res.sendRedirect(
                req.getContextPath() + "/baitolog/Login.action"
            );
            return;
        }

        Student student = (Student) loginUser;

        if (!student.isAuthenticated()) {
            res.sendRedirect(
                req.getContextPath() + "/baitolog/Login.action"
            );
            return;
        }

        // --------------------------------------------------
        // 2. 入力値を取得
        // --------------------------------------------------

        String workplaceIdText =
                req.getParameter("workplace_id");

        String workDateText =
                req.getParameter("work_date");

        String startTimeText =
                req.getParameter("start_time");

        String endTimeText =
                req.getParameter("end_time");

        String breakStartText =
                req.getParameter("break_start");

        String breakEndText =
                req.getParameter("break_end");

        // --------------------------------------------------
        // 3. 入力値を検証
        // --------------------------------------------------

        if (isEmpty(workplaceIdText)
                || isEmpty(workDateText)
                || isEmpty(startTimeText)
                || isEmpty(endTimeText)) {

            showError(req, res, student,
                    "勤務先・勤務日・開始時刻・終了時刻を入力してください。");
            return;
        }

        int workplaceId;
        LocalDate workDate;
        LocalTime startTime;
        LocalTime endTime;
        LocalTime breakStart = null;
        LocalTime breakEnd = null;

        try {
            workplaceId = Integer.parseInt(workplaceIdText);
            workDate = LocalDate.parse(workDateText);
            startTime = LocalTime.parse(startTimeText);
            endTime = LocalTime.parse(endTimeText);

            if (!isEmpty(breakStartText)) {
                breakStart = LocalTime.parse(breakStartText);
            }

            if (!isEmpty(breakEndText)) {
                breakEnd = LocalTime.parse(breakEndText);
            }

        } catch (Exception e) {
            showError(req, res, student,
                    "日付または時刻の入力内容を確認してください。");
            return;
        }

        // 開始と終了が同じ時刻の場合は認めない
        if (startTime.equals(endTime)) {
            showError(req, res, student,
                    "開始時刻と終了時刻を異なる時刻にしてください。");
            return;
        }

        // 休憩開始・終了は両方入力するか、両方空欄にする
        if ((breakStart == null) != (breakEnd == null)) {
            showError(req, res, student,
                    "休憩を設定する場合は、休憩開始と休憩終了の両方を入力してください。");
            return;
        }

        // --------------------------------------------------
        // 4. 本人の勤務先か確認
        // --------------------------------------------------

        WorkplaceDao workplaceDao = new WorkplaceDao();

        Workplace workplace =
                workplaceDao.findById(workplaceId, student.getId());

        if (workplace == null) {
            showError(req, res, student,
                    "選択した勤務先が見つかりません。");
            return;
        }

        // --------------------------------------------------
        // 5. 仮シフトを作成
        // --------------------------------------------------

        Shift candidate = new Shift();

        candidate.setStudentId(student.getId());
        candidate.setWorkplaceId(workplaceId);
        candidate.setWorkDate(workDate);
        candidate.setStartTime(startTime);
        candidate.setEndTime(endTime);
        candidate.setBreakStart(breakStart);
        candidate.setBreakEnd(breakEnd);

        // 休憩時間が勤務時間内にあるか確認
        if (candidate.hasBreak()) {

            long workMinutes =
                    minutesBetween(startTime, endTime);

            long breakStartMinutes =
                    minutesBetween(startTime, breakStart);

            long breakEndMinutes =
                    minutesBetween(startTime, breakEnd);

            if (breakEndMinutes <= breakStartMinutes
                    || breakStartMinutes < 0
                    || breakEndMinutes > workMinutes) {

                showError(req, res, student,
                        "休憩時間は勤務開始から終了までの範囲内に設定してください。");
                return;
            }
        }

        if (candidate.getWorkHours() <= 0) {
            showError(req, res, student,
                    "労働時間が0時間以下になっています。入力内容を確認してください。");
            return;
        }

        // --------------------------------------------------
        // 6. 既存シフトを取得
        // --------------------------------------------------

        LocalDate searchFrom = workDate.minusDays(6);
        LocalDate searchTo = workDate.plusDays(6);

        ShiftDao shiftDao = new ShiftDao();

        List<Shift> shifts =
                new ArrayList<>(
                    shiftDao.filter(student, searchFrom, searchTo)
                );

        // 仮シフトを追加する（DBには保存しない）
        shifts.add(candidate);

        // --------------------------------------------------
        // 7. 勤務先一覧を取得
        // --------------------------------------------------

        List<Workplace> workplaces =
                workplaceDao.findByStudent(student.getId());

        Map<Integer, String> workplaceNames =
                new LinkedHashMap<>();

        for (Workplace w : workplaces) {
            workplaceNames.put(
                w.getId(),
                w.getDisplayName()
            );
        }

        // --------------------------------------------------
        // 8. 長期休暇期間と上限を取得
        // --------------------------------------------------

        if (student.getSchool() == null) {
            showError(req, res, student,
                    "所属学校の情報を取得できません。ログイン情報を確認してください。");
            return;
        }

        LongBreakDao longBreakDao = new LongBreakDao();

        List<LongBreak> longBreaks =
                longBreakDao.filter(
                    student.getSchool(),
                    searchFrom,
                    searchTo
                );

        LimitPolicy policy =
                new LimitPolicy(
                    student.getSchool(),
                    longBreaks
                );

        // --------------------------------------------------
        // 9. 仮シフトを含む7日間をすべて判定
        // --------------------------------------------------

        WindowResult worstResult = null;

        double worstOverHours = -1;
        double worstRate = -1;

        for (int i = 0; i <= 6; i++) {

            LocalDate from = workDate.minusDays(6 - i);
            LocalDate to = from.plusDays(6);

            double totalHours = 0;

            Map<String, Double> workplaceHours =
                    new LinkedHashMap<>();

            for (Shift shift : shifts) {

                LocalDate date = shift.getWorkDate();

                if (date.isBefore(from) || date.isAfter(to)) {
                    continue;
                }

                double hours = shift.getWorkHours();

                totalHours += hours;

                String workplaceName =
                        workplaceNames.get(shift.getWorkplaceId());

                if (workplaceName == null) {
                    workplaceName =
                            "勤務先ID " + shift.getWorkplaceId();
                }

                workplaceHours.merge(
                    workplaceName,
                    hours,
                    Double::sum
                );
            }

            double limitHours =
                    policy.getLimitHours(from, to);

            WindowResult result = new WindowResult();

            result.setStartDate(from);
            result.setEndDate(to);
            result.setTotalHours(
                Math.round(totalHours * 100.0) / 100.0
            );
            result.setLimitHours(limitHours);
            result.setWorkplaceHours(workplaceHours);

            double overHours =
                    Math.max(0, totalHours - limitHours);

            double rate =
                    limitHours <= 0 ? 0 : totalHours / limitHours;

            // 上限超過が大きい期間を優先する。
            // 超過がない場合は、上限に対する割合が高い期間を選ぶ。
            if (worstResult == null
                    || overHours > worstOverHours
                    || (overHours == worstOverHours
                        && rate > worstRate)) {

                worstResult = result;
                worstOverHours = overHours;
                worstRate = rate;
            }
        }

        // --------------------------------------------------
        // 10. 判定結果をJSPへ渡す
        // --------------------------------------------------

        req.setAttribute("result", worstResult);
        req.setAttribute("candidate", candidate);
        req.setAttribute("candidateWorkplace", workplace);
        req.setAttribute("workplaceNames", workplaceNames);

        req.setAttribute(
            "candidateHours",
            candidate.getWorkHours()
        );

        if (worstResult.isOver()) {
            req.setAttribute(
                "judgeMessage",
                "このシフトを受け入れると、7日間の労働時間上限を超えます。"
            );
            req.setAttribute("judgeClass", "danger");

        } else if ("CAUTION".equals(worstResult.getStatus())) {
            req.setAttribute(
                "judgeMessage",
                "上限以内ですが、労働時間が上限に近づいています。"
            );
            req.setAttribute("judgeClass", "warning");

        } else {
            req.setAttribute(
                "judgeMessage",
                "このシフトを受け入れても、7日間の労働時間上限内です。"
            );
            req.setAttribute("judgeClass", "success");
        }

        // --------------------------------------------------
        // 11. 結果画面へ
        // --------------------------------------------------

        req.getRequestDispatcher(
            "/baitolog/student/simulate_result.jsp"
        ).forward(req, res);
    }

    /**
     * 入力エラー時に入力画面へ戻す
     */
    private void showError(
            HttpServletRequest req,
            HttpServletResponse res,
            Student student,
            String message) throws Exception {

        WorkplaceDao workplaceDao = new WorkplaceDao();

        req.setAttribute(
            "workplaces",
            workplaceDao.findByStudent(student.getId())
        );

        req.setAttribute("errorMessage", message);

        req.getRequestDispatcher(
            "/baitolog/student/simulate.jsp"
        ).forward(req, res);
    }

    /**
     * nullまたは空文字か確認
     */
    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 日をまたぐ勤務時間にも対応して分数を計算する
     */
    private long minutesBetween(
            LocalTime from,
            LocalTime to) {

        long minutes = Duration.between(from, to).toMinutes();

        if (minutes < 0) {
            minutes += 24 * 60;
        }

        return minutes;
    }
}
