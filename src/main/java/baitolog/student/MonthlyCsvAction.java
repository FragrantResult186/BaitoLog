package baitolog.student;

import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.Shift;
import bean.Student;
import bean.User;
import bean.Workplace;

import dao.ShiftDao;
import dao.WorkplaceDao;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import tool.Action;

/**
 * 月次勤務記録CSV出力
 */
public class MonthlyCsvAction extends Action {

    private static final String[] WEEKDAYS = {
        "月", "火", "水", "木", "金", "土", "日"
    };

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

        // -----------------------------
        // 2. 対象年月
        // -----------------------------

        YearMonth targetMonth;

        try {

            String month =
                req.getParameter("month");

            targetMonth =
                month == null || month.isBlank()
                ? YearMonth.now()
                : YearMonth.parse(month);

        } catch (Exception e) {

            res.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "対象年月が正しくありません。"
            );

            return;
        }

        LocalDate start =
            targetMonth.atDay(1);

        LocalDate end =
            targetMonth.atEndOfMonth();

        // -----------------------------
        // 3. DBから取得
        // -----------------------------

        List<Shift> shifts =
            shiftDao.filter(
                student,
                start,
                end
            );

        List<Workplace> workplaces =
            workplaceDao.findByStudent(
                student.getId()
            );

        Map<Integer, Workplace> workplaceMap =
            new HashMap<>();

        for (Workplace workplace : workplaces) {

            workplaceMap.put(
                workplace.getId(),
                workplace
            );
        }

        // -----------------------------
        // 4. CSV出力設定
        // -----------------------------

        String fileName =
            "BaitoLog_"
            + targetMonth.format(
                DateTimeFormatter.ofPattern("yyyyMM")
            )
            + ".csv";

        String encodedFileName =
            URLEncoder.encode(
                fileName,
                StandardCharsets.UTF_8
            ).replace("+", "%20");

        res.setCharacterEncoding("UTF-8");

        res.setContentType(
            "text/csv; charset=UTF-8"
        );

        res.setHeader(
            "Content-Disposition",
            "attachment; filename*=UTF-8''"
                + encodedFileName
        );

        // -----------------------------
        // 5. CSVデータ出力
        // -----------------------------

        try (
            PrintWriter writer =
                res.getWriter()
        ) {

            // Excelで文字化けしないようBOMを付ける
            writer.write('\uFEFF');

            // ヘッダー
            writer.println(
                "日付,曜日,勤務先,開始時刻,終了時刻,"
                + "休憩開始,休憩終了,労働時間"
            );

            double totalHours = 0;

            for (Shift shift : shifts) {

                Workplace workplace =
                    workplaceMap.get(
                        shift.getWorkplaceId()
                    );

                String workplaceName =
                    workplace == null
                    ? "勤務先不明"
                    : workplace.getDisplayName();

                LocalDate date =
                    shift.getWorkDate();

                String weekday =
                    WEEKDAYS[
                        date.getDayOfWeek().getValue() - 1
                    ];

                double hours =
                    shift.getWorkHours();

                totalHours += hours;

                writer.println(
                    csv(date.toString()) + ","
                    + csv(weekday) + ","
                    + csv(workplaceName) + ","
                    + csv(time(shift.getStartTime())) + ","
                    + csv(time(shift.getEndTime())) + ","
                    + csv(time(shift.getBreakStart())) + ","
                    + csv(time(shift.getBreakEnd())) + ","
                    + csv(
                        String.format(
                            java.util.Locale.ROOT,
                            "%.2f",
                            hours
                        )
                    )
                );
            }

            writer.println();

            writer.println(
                "月合計労働時間,,,,,,,"
                + String.format(
                    java.util.Locale.ROOT,
                    "%.2f",
                    totalHours
                )
            );

            writer.flush();
        }
    }

    /**
     * CSV用エスケープ
     */
    private String csv(String value) {

        if (value == null) {
            value = "";
        }

        return "\""
            + value.replace("\"", "\"\"")
            + "\"";
    }

    /**
     * 時刻をHH:mm形式にする。
     */
    private String time(
            java.time.LocalTime time
    ) {

        if (time == null) {
            return "";
        }

        return time.format(
            DateTimeFormatter.ofPattern("HH:mm")
        );
    }
}
