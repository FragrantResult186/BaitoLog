package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import bean.Report;
import bean.ReportItem;
import bean.Student;

/**
 * 報告書（REPORT・REPORT_ITEM）を扱うDao
 * 報告書は提出したときに1回だけ保存し、あとから変えない（提出時点の内容を残す）
 */
public class ReportDao extends Dao {

	/**
	 * getメソッド 報告書を明細つきで1件取得する
	 *
	 * @param student:Student
	 *            学生
	 * @param targetMonth:LocalDate
	 *            対象年月（その月のどの日でもよい。1日に直して探す）
	 * @return 報告書 まだ提出していない場合はnull
	 * @throws Exception
	 */
	public Report get(Student student, LocalDate targetMonth) throws Exception {
		Report report = null;

		try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"select * from REPORT where STUDENT_ID = ? and TARGET_MONTH = ?");
				PreparedStatement itemStatement = connection.prepareStatement(
						"select * from REPORT_ITEM where REPORT_ID = ? order by WORK_DATE, START_TIME")) {
			statement.setInt(1, student.getId());
			statement.setObject(2, targetMonth.withDayOfMonth(1));
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				report = new Report();
				report.setId(resultSet.getInt("ID"));
				report.setStudent(student);
				report.setTargetMonth(resultSet.getObject("TARGET_MONTH", LocalDate.class));
				report.setStatus(resultSet.getString("STATUS"));
				report.setSubmittedAt(resultSet.getObject("SUBMITTED_AT", LocalDateTime.class));
				report.setTotalHours(resultSet.getDouble("TOTAL_HOURS"));
				report.setMax7dayHours(resultSet.getDouble("MAX_7DAY_HOURS"));
				report.setMax7dayStart(resultSet.getObject("MAX_7DAY_START", LocalDate.class));
				report.setWorkplaceCount(resultSet.getInt("WORKPLACE_COUNT"));

				// 明細を取得
				itemStatement.setInt(1, report.getId());
				ResultSet itemResult = itemStatement.executeQuery();
				List<ReportItem> items = new ArrayList<>();
				while (itemResult.next()) {
					ReportItem item = new ReportItem();
					item.setId(itemResult.getInt("ID"));
					item.setWorkDate(itemResult.getObject("WORK_DATE", LocalDate.class));
					item.setWorkplaceName(itemResult.getString("WORKPLACE_NAME"));
					item.setStartTime(itemResult.getObject("START_TIME", LocalTime.class));
					item.setEndTime(itemResult.getObject("END_TIME", LocalTime.class));
					item.setBreakStart(itemResult.getObject("BREAK_START", LocalTime.class));
					item.setBreakEnd(itemResult.getObject("BREAK_END", LocalTime.class));
					item.setWorkingHours(itemResult.getDouble("WORKING_HOURS"));
					items.add(item);
				}
				report.setItems(items);
			}
		}

		return report;
	}

	/**
	 * saveメソッド 報告書と明細を登録する（提出）
	 * 1つのトランザクションで行い、失敗したら全部取り消す
	 *
	 * @param report:Report
	 *            報告書（student・items がセットされていること）
	 * @return 成功:true, その月はもう提出済みだった:false
	 * @throws Exception
	 */
	public boolean save(Report report) throws Exception {
		try (Connection connection = getConnection()) {
			connection.setAutoCommit(false);
			try (PreparedStatement statement = connection.prepareStatement(
					"insert into REPORT (STUDENT_ID, TARGET_MONTH, STATUS, SUBMITTED_AT, TOTAL_HOURS, "
							+ "MAX_7DAY_HOURS, MAX_7DAY_START, WORKPLACE_COUNT) values (?, ?, ?, ?, ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
					PreparedStatement itemStatement = connection.prepareStatement(
							"insert into REPORT_ITEM (REPORT_ID, WORK_DATE, WORKPLACE_NAME, START_TIME, END_TIME, "
									+ "BREAK_START, BREAK_END, WORKING_HOURS) values (?, ?, ?, ?, ?, ?, ?, ?)")) {
				// 報告書を登録
				statement.setInt(1, report.getStudent().getId());
				statement.setObject(2, report.getTargetMonth().withDayOfMonth(1));
				statement.setString(3, report.getStatus());
				statement.setObject(4, report.getSubmittedAt());
				statement.setDouble(5, report.getTotalHours());
				statement.setDouble(6, report.getMax7dayHours());
				statement.setObject(7, report.getMax7dayStart());
				statement.setInt(8, report.getWorkplaceCount());
				statement.executeUpdate();
				ResultSet keys = statement.getGeneratedKeys();
				keys.next();
				report.setId(keys.getInt(1));

				// 明細を登録
				for (ReportItem item : report.getItems()) {
					itemStatement.setInt(1, report.getId());
					itemStatement.setObject(2, item.getWorkDate());
					itemStatement.setString(3, item.getWorkplaceName());
					itemStatement.setObject(4, item.getStartTime());
					itemStatement.setObject(5, item.getEndTime());
					itemStatement.setObject(6, item.getBreakStart());
					itemStatement.setObject(7, item.getBreakEnd());
					itemStatement.setDouble(8, item.getWorkingHours());
					itemStatement.executeUpdate();
				}

				connection.commit();
			} catch (SQLException e) {
				connection.rollback();
				// 23505 = 一意制約違反（同じ月の報告書がもうある）
				if ("23505".equals(e.getSQLState())) {
					report.setId(0);
					return false;
				}
				throw e;
			} catch (Exception e) {
				connection.rollback();
				throw e;
			}
		}

		return true;
	}
}
