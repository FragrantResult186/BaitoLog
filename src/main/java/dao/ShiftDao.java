package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.Shift;
import bean.Student;
import bean.Workplace;

/**
 * 
 * シフト・勤務記録（SHIFT）を扱うDao
 * 
 */
public class ShiftDao extends Dao {

	/**
	 * シフトを勤務先つきで取るSQL
	 */
	private String baseSql =
			  "select S.*, W.NAME as W_NAME, W.COLOR as W_COLOR "
			+ "from SHIFT S join WORKPLACE W on W.ID = S.WORKPLACE_ID ";

	/**
	 * postFilterメソッド リザルトセットをシフトのリストにする
	 *
	 * @param resultSet:ResultSet
	 * @param students:Map<Integer, Student> 学生ID → 学生
	 * @return シフトのリスト:List<Shift>
	 * @throws Exception
	 */
	private List<Shift> postFilter(ResultSet resultSet, Map<Integer, Student> students) throws Exception {
		List<Shift> list = new ArrayList<>();
		// 同じ勤務先は同じインスタンスを使う
		Map<Integer, Workplace> workplaces = new HashMap<>();

		while (resultSet.next()) {
			Student student = students.get(resultSet.getInt("STUDENT_ID"));

			int workplaceId = resultSet.getInt("WORKPLACE_ID");
			Workplace workplace = workplaces.get(workplaceId);
			if (workplace == null) {
				workplace = new Workplace();
				workplace.setId(workplaceId);
				workplace.setName(resultSet.getString("W_NAME"));
				workplace.setColor(resultSet.getString("W_COLOR"));
				workplaces.put(workplaceId, workplace);
			}

			Shift shift = new Shift();
			shift.setId(resultSet.getInt("ID"));
			shift.setStudentId(student.getId());
			shift.setWorkplaceId(workplaceId);
			shift.setWorkDate(resultSet.getObject("WORK_DATE", LocalDate.class));
			shift.setStartTime(resultSet.getObject("START_TIME", LocalTime.class));
			shift.setEndTime(resultSet.getObject("END_TIME", LocalTime.class));
			shift.setBreakStart(resultSet.getObject("BREAK_START", LocalTime.class));
			shift.setBreakEnd(resultSet.getObject("BREAK_END", LocalTime.class));
			list.add(shift);
		}
		return list;
	}

	/**
	 * getメソッド シフトを1件取得する
	 *
	 * @param id:int シフトID
	 * @param student:Student ログイン中の学生
	 * @return シフト 存在しない・ほかの人のシフトの場合はnull
	 * @throws Exception
	 */
	public Shift get(int id, Student student) throws Exception {
		List<Shift> list;

		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(
						baseSql + "where S.ID = ? and S.STUDENT_ID = ?"))
		{
			statement.setInt(1, id);
			statement.setInt(2, student.getId());
			list = postFilter(statement.executeQuery(), Map.of(student.getId(), student));
		}

		return list.isEmpty() ? null : list.get(0);
	}

	/**
	 * filterメソッド 1人の学生の、期間内のシフトを取得する
	 * 7日間判定をするときは、期間の開始日を「月初の6日前」のように前へ広げて渡すこと
	 *
	 * @param student:Student 学生
	 * @param start:LocalDate 開始日
	 * @param end:LocalDate 終了日
	 * @return シフトのリスト:List<Shift>
	 * @throws Exception
	 */
	public List<Shift> filter(Student student, LocalDate start, LocalDate end) throws Exception {
		List<Shift> list;

		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(
						baseSql + "where S.STUDENT_ID = ? and S.WORK_DATE between ? and ? "
								+ "order by S.WORK_DATE, S.START_TIME"))
		{
			statement.setInt(1, student.getId());
			statement.setObject(2, start);
			statement.setObject(3, end);
			list = postFilter(statement.executeQuery(), Map.of(student.getId(), student));
		}

		return list;
	}

	/**
	 * filterメソッド 複数の学生の、期間内のシフトをまとめて取得する（学生一覧用）
	 * 学生ごとに分けるときは shift.getStudent() を見る
	 *
	 * @param students:List<Student> 学生のリスト
	 * @param start:LocalDate 開始日
	 * @param end:LocalDate 終了日
	 * @return シフトのリスト:List<Shift>
	 * @throws Exception
	 */
	public List<Shift> filter(List<Student> students, LocalDate start, LocalDate end) throws Exception {
		if (students.isEmpty()) {
			return new ArrayList<>();
		}

		// 学生ID → 学生
		Map<Integer, Student> map = new HashMap<>();
		for (Student student : students) {
			map.put(student.getId(), student);
		}
		// 「?, ?, ?」を人数分つくる
		String marks = String.join(", ", Collections.nCopies(students.size(), "?"));
		List<Shift> list;

		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(
						baseSql + "where S.STUDENT_ID in (" + marks + ") and S.WORK_DATE between ? and ? "
								+ "order by S.STUDENT_ID, S.WORK_DATE, S.START_TIME"))
		{
			int index = 1;
			for (Student student : students) {
				statement.setInt(index++, student.getId());
			}
			statement.setObject(index++, start);
			statement.setObject(index, end);
			list = postFilter(statement.executeQuery(), map);
		}

		return list;
	}

	/**
	 * countメソッド 勤務先に登録されているシフトの件数を数える
	 *
	 * @param workplace:Workplace 勤務先
	 * @return 件数:int
	 * @throws Exception
	 */
	public int count(Workplace workplace) throws Exception {
		int count = 0;

		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement("select count(*) from SHIFT where WORKPLACE_ID = ?"))
		{
			statement.setInt(1, workplace.getId());
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				count = resultSet.getInt(1);
			}
		}

		return count;
	}

	/**
	 * saveメソッド シフトを保存する
	 *
	 * @param shift:Shift シフト
	 * @return 成功:true ほかの人のシフトだった:false
	 * @throws Exception
	 */
	public boolean save(Shift shift) throws Exception {
		int count;

		try (Connection connection = getConnection()) {
			if (shift.getId() == 0) {
				// 登録
				try (PreparedStatement statement = connection.prepareStatement(
						"insert into SHIFT (STUDENT_ID, WORKPLACE_ID, WORK_DATE, START_TIME, END_TIME, BREAK_START, BREAK_END) "
					  + "values (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS))
				{
					statement.setInt(1, shift.getStudentId());
					statement.setInt(2, shift.getWorkplaceId());
					statement.setObject(3, shift.getWorkDate());
					statement.setObject(4, shift.getStartTime());
					statement.setObject(5, shift.getEndTime());
					statement.setObject(6, shift.getBreakStart());
					statement.setObject(7, shift.getBreakEnd());
					count = statement.executeUpdate();
					// 自動で振られたIDをシフトにセット
					ResultSet keys = statement.getGeneratedKeys();
					if (keys.next()) {
						shift.setId(keys.getInt(1));
					}
				}
			} else {
				// 更新
				try (PreparedStatement statement = connection.prepareStatement(
						"update SHIFT set WORKPLACE_ID = ?, WORK_DATE = ?, START_TIME = ?, END_TIME = ?, "
					  + "BREAK_START = ?, BREAK_END = ? where ID = ? and STUDENT_ID = ?"))
				{
					statement.setInt(1, shift.getWorkplaceId());
					statement.setObject(2, shift.getWorkDate());
					statement.setObject(3, shift.getStartTime());
					statement.setObject(4, shift.getEndTime());
					statement.setObject(5, shift.getBreakStart());
					statement.setObject(6, shift.getBreakEnd());
					statement.setInt(7, shift.getId());
					statement.setInt(8, shift.getStudentId());
					count = statement.executeUpdate();
				}
			}
		}

		return count > 0;
	}

	/**
	 * deleteメソッド シフトを削除する
	 *
	 * @param shift:Shift シフト
	 * @return 成功:true, 失敗:false
	 * @throws Exception
	 */
	public boolean delete(Shift shift) throws Exception {
		int count;

		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement("delete from SHIFT where ID = ? and STUDENT_ID = ?"))
		{
			statement.setInt(1, shift.getId());
			statement.setInt(2, shift.getStudentId());
			count = statement.executeUpdate();
		}

		return count > 0;
	}
}
