package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import bean.ShiftTemplate;
import bean.Workplace;

/**
 * 勤務先DAO（テーブル WORKPLACE）
 * 勤務先とシフトテンプレートの保存・削除は、1つのトランザクションで行う
 */
public class WorkplaceDao extends Dao {

	/**
	 * findByStudentメソッド 学生の勤務先をすべて取得する
	 *
	 * @param studentId:int 学生のユーザーID
	 * @return 勤務先のリスト:List<Workplace>（登録順）
	 * @throws Exception
	 */
	public List<Workplace> findByStudent(int studentId) throws Exception {
		List<Workplace> list = new ArrayList<>();

		try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"select id, student_id, name, branch_name, color "
								+ "from workplace where student_id = ? order by id")) {
			statement.setInt(1, studentId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					list.add(toBean(resultSet));
				}
			}
		}
		return list;
	}

	/**
	 * findByIdメソッド 勤務先を1件取得する（他の学生の勤務先は取得できない）
	 *
	 * @param id:int 勤務先ID
	 * @param studentId:int 学生のユーザーID
	 * @return 勤務先:Workplace、なければ null
	 * @throws Exception
	 */
	public Workplace findById(int id, int studentId) throws Exception {
		Workplace workplace = null;

		try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"select id, student_id, name, branch_name, color "
								+ "from workplace where id = ? and student_id = ?")) {
			statement.setInt(1, id);
			statement.setInt(2, studentId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					workplace = toBean(resultSet);
				}
			}
		}
		return workplace;
	}

	/**
	 * hasShiftメソッド 勤務先にシフトが登録されているか調べる
	 *
	 * @param id:int 勤務先ID
	 * @return true:登録済み（削除できない）
	 * @throws Exception
	 */
	public boolean hasShift(int id) throws Exception {
		try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"select count(*) from shift where workplace_id = ?")) {
			statement.setInt(1, id);
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				return resultSet.getInt(1) > 0;
			}
		}
	}

	/**
	 * saveメソッド 勤務先とテンプレートを保存する（IDが0なら新規登録、それ以外は更新）
	 * テンプレートは、いったん全部消して、渡されたものを登録し直す
	 *
	 * @param workplace:Workplace 勤務先
	 * @param templates:List<ShiftTemplate> テンプレート
	 * @return 保存した勤務先のID:int
	 * @throws Exception
	 */
	public int save(Workplace workplace, List<ShiftTemplate> templates) throws Exception {
		try (Connection connection = getConnection()) {
			connection.setAutoCommit(false);
			try {
				int id = workplace.getId();
				if (id == 0) {
					try (PreparedStatement statement = connection.prepareStatement(
							"insert into workplace (student_id, name, branch_name, color) values (?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS)) {
						statement.setInt(1, workplace.getStudentId());
						statement.setString(2, workplace.getName());
						statement.setString(3, emptyToNull(workplace.getBranchName()));
						statement.setString(4, emptyToNull(workplace.getColor()));
						statement.executeUpdate();
						try (ResultSet keys = statement.getGeneratedKeys()) {
							keys.next();
							id = keys.getInt(1);
						}
					}
				} else {
					try (PreparedStatement statement = connection.prepareStatement(
							"update workplace set name = ?, branch_name = ?, color = ? "
									+ "where id = ? and student_id = ?")) {
						statement.setString(1, workplace.getName());
						statement.setString(2, emptyToNull(workplace.getBranchName()));
						statement.setString(3, emptyToNull(workplace.getColor()));
						statement.setInt(4, id);
						statement.setInt(5, workplace.getStudentId());
						statement.executeUpdate();
					}
					deleteTemplates(connection, id);
				}

				try (PreparedStatement statement = connection.prepareStatement(
						"insert into shift_template (workplace_id, name, start_time, end_time, break_min) "
								+ "values (?, ?, ?, ?, ?)")) {
					for (ShiftTemplate template : templates) {
						statement.setInt(1, id);
						statement.setString(2, template.getName());
						statement.setTime(3, Time.valueOf(LocalTime.parse(template.getStartTime())));
						statement.setTime(4, Time.valueOf(LocalTime.parse(template.getEndTime())));
						statement.setInt(5, template.getBreakMin());
						statement.executeUpdate();
					}
				}
				connection.commit();
				return id;
			} catch (Exception e) {
				connection.rollback();
				throw e;
			}
		}
	}

	/**
	 * deleteメソッド 勤務先とそのテンプレートを削除する
	 *
	 * @param id:int 勤務先ID
	 * @param studentId:int 学生のユーザーID
	 * @return 削除した勤務先の行数:int（1:成功）
	 * @throws Exception
	 */
	public int delete(int id, int studentId) throws Exception {
		try (Connection connection = getConnection()) {
			connection.setAutoCommit(false);
			try {
				deleteTemplates(connection, id);
				int count;
				try (PreparedStatement statement = connection.prepareStatement(
						"delete from workplace where id = ? and student_id = ?")) {
					statement.setInt(1, id);
					statement.setInt(2, studentId);
					count = statement.executeUpdate();
				}
				connection.commit();
				return count;
			} catch (Exception e) {
				connection.rollback();
				throw e;
			}
		}
	}

	private void deleteTemplates(Connection connection, int workplaceId) throws Exception {
		try (PreparedStatement statement = connection.prepareStatement(
				"delete from shift_template where workplace_id = ?")) {
			statement.setInt(1, workplaceId);
			statement.executeUpdate();
		}
	}

	private Workplace toBean(ResultSet resultSet) throws Exception {
		Workplace workplace = new Workplace();
		workplace.setId(resultSet.getInt("id"));
		workplace.setStudentId(resultSet.getInt("student_id"));
		workplace.setName(resultSet.getString("name"));
		workplace.setBranchName(resultSet.getString("branch_name"));
		workplace.setColor(resultSet.getString("color"));
		return workplace;
	}

	private String emptyToNull(String value) {
		return (value == null || value.isEmpty()) ? null : value;
	}
}
