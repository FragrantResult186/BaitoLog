package baitolog.student;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import bean.LongBreak;
import bean.MonthlySummary;
import bean.Shift;
import bean.Student;
import bean.User;
import bean.WindowResult;
import bean.Workplace;

import dao.LongBreakDao;
import dao.ShiftDao;
import dao.WorkplaceDao;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import logic.LimitPolicy;
import logic.SevenDayJudge;

import tool.Action;

/**
 * 月次記録画面
 */
public class MonthlyAction extends Action {

    @Override
    public void execute(
            HttpServletRequest req,
            HttpServletResponse res
    ) throws Exception {

        // -----------------------------
        // 1. ローカル変数
        // -----------------------------

        HttpSession session =
            req.getSession(false);

        String root =
            req.getContextPath() + "/baitolog";

        if (session == null) {

            res.sendRedirect(root + "/Login.action");
            return;
        }

        User user =
            (User) session.getAttribute("user");

        if (
            !(user instanceof Student)
            || !user.isAuthenticated()
        ) {

            res.sendRedirect(root + "/Login.action");
            return;
        }

        Student student =
            (Student) user;

        ShiftDao shiftDao =
            new ShiftDao();

        WorkplaceDao workplaceDao =
            new WorkplaceDao();

        LongBreakDao longBreakDao =
            new LongBreakDao();

        SevenDayJudge judge =
            new SevenDayJudge();

        // -----------------------------
        // 2. 対象年月の取得
        // -----------------------------

        YearMonth targetMonth;

        String monthText =
            req.getParameter("month");

        try {

            targetMonth = monthText == null
                || monthText.isBlank()
                ? YearMonth.now()
                : YearMonth.parse(monthText);

        } catch (Exception e) {

            targetMonth = YearMonth.now();
        }

        LocalDate monthStart =
            targetMonth.atDay(1);

        LocalDate monthEnd =
            targetMonth.atEndOfMonth();

        // -----------------------------
        // 3. DBからデータ取得
        // -----------------------------

        List<Workplace> workplaces =
            workplaceDao.findByStudent(
                student.getId()
            );

        // 月の前後6日間も取得する。
        // 月をまたぐ7日間判定に必要。
        LocalDate searchStart =
            monthStart.minusDays(6);

        LocalDate searchEnd =
            monthEnd.plusDays(6);

        List<Shift> allShifts =
            shiftDao.filter(
                student,
                searchStart,
                searchEnd
            );

        List<LongBreak> longBreaks =
            longBreakDao.filter(
                student.getSchool(),
                searchStart,
                searchEnd
            );

        // -----------------------------
        // 4. ビジネスロジック
        // -----------------------------

        // 月内のシフトだけを抽出
        List<Shift> monthlyShifts =
            new ArrayList<>();

        double totalHours = 0;

        Map<Integer, String> workplaceNames =
            new LinkedHashMap<>();

        for (Workplace workplace : workplaces) {

            workplaceNames.put(
                workplace.getId(),
                workplace.getDisplayName()
            );
        }

        Map<String, Double> workplaceHours =
            new LinkedHashMap<>();

        for (Shift shift : allShifts) {

            LocalDate date =
                shift.getWorkDate();

            if (
                date.isBefore(monthStart)
                || date.isAfter(monthEnd)
            ) {
                continue;
            }

            monthlyShifts.add(shift);

            double hours =
                shift.getWorkHours();

            totalHours += hours;

            String name =
                workplaceNames.getOrDefault(
                    shift.getWorkplaceId(),
                    "勤務先不明"
                );

            workplaceHours.merge(
                name,
                hours,
                Double::sum
            );
        }

        // 小数点第2位まで
        totalHours =
            Math.round(totalHours * 100) / 100.0;

        // 7日間判定
        LimitPolicy policy =
            new LimitPolicy(
                student.getSchool(),
                longBreaks
            );

        double maxHours = 0;

        LocalDate maxStart = null;

        LocalDate maxEnd = null;

        // 月内の日付を含む7日間をすべて判定
        for (
            LocalDate start = searchStart;
            !start.isAfter(monthEnd);
            start = start.plusDays(1)
        ) {

            LocalDate end =
                start.plusDays(6);

            WindowResult result =
                judge.judgeWindow(
                    allShifts,
                    workplaces,
                    start,
                    end,
                    policy
                );

            if (
                result.getTotalHours() > maxHours
                || maxStart == null
            ) {

                maxHours =
                    result.getTotalHours();

                maxStart = start;

                maxEnd = end;
            }
        }

        maxHours =
            Math.round(maxHours * 100) / 100.0;

        // -----------------------------
        // 5. MonthlySummary作成
        // -----------------------------

        MonthlySummary summary =
            new MonthlySummary();

        summary.setMonth(monthStart);

        summary.setShifts(monthlyShifts);

        summary.setTotalHours(totalHours);

        summary.setMax7dayHours(maxHours);

        summary.setMax7dayStart(maxStart);

        summary.setMax7dayEnd(maxEnd);

        summary.setWorkplaceHours(
            workplaceHours
        );

        // -----------------------------
        // 6. JSPへ渡す
        // -----------------------------

        req.setAttribute(
            "summary",
            summary
        );

        req.setAttribute(
            "month",
            targetMonth.toString()
        );

        req.setAttribute(
            "monthLabel",
            targetMonth.getYear()
                + "年"
                + targetMonth.getMonthValue()
                + "月"
        );

        req.setAttribute(
            "monthlyShifts",
            monthlyShifts
        );

        req.setAttribute(
            "workplaces",
            workplaces
        );

        req.setAttribute(
            "workplaceNames",
            workplaceNames
        );

        req.setAttribute(
            "prevMonth",
            targetMonth.minusMonths(1).toString()
        );

        req.setAttribute(
            "nextMonth",
            targetMonth.plusMonths(1).toString()
        );

        // -----------------------------
        // 7. JSPへフォワード
        // -----------------------------

        req.getRequestDispatcher(
            "monthly.jsp"
        ).forward(req, res);
    }
}
