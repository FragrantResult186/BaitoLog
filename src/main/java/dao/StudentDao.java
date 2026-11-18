package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bean.ImportRow;
import bean.School;
import bean.SchoolClass;
import bean.Student;
import bean.User;
import logic.PasswordUtil;

/**
 * 
 * 学生（STUDENT・CLASS_ENROLLMENT）を扱うDao
 * 
 */
public class StudentDao extends Dao {

	/**
	 * 学生を取るSQL
	 */
	private String baseSql = 
			  "select U.ID, U.LOGIN_ID, U.NAME, U.ROLE, U.PASSWORD_INITIALIZED, S.BIRTHDAY, "
			+ "CL.ID as CLASS_ID, CL.NAME as CLASS_NAME "
			+ "from STUDENT S "
			+ "join USERS U on U.ID = S.USER_ID "
			+ "left join CLASS_ENROLLMENT CE on CE.STUDENT_ID = S.USER_ID "
			+ "and CE.START_DATE <= ? and (CE.END_DATE is null or CE.END_DATE >= ?) "
			+ "left join SCHOOL_CLASS CL on CL.ID = CE.SCHOOL_CLASS_ID "
			+ "where S.SCHOOL_ID = ? ";

	/**
	 * postFilterメソッド リザルトセットを学生のリストにする
	 *
	 * @param resultSet:ResultSet
	 * @param school:School 生徒の学校
	 * @return 学生のリスト:List<Student>
	 * @throws Exception
	 */
	private List<Student> postFilter(ResultSet resultSet, School school) throws Exception {
		List<Student> list = new ArrayList<>();
		while (resultSet.next()) {
			// リストの作成
			Student student = new Student();
			student.setId(resultSet.getInt("ID"));
			student.setLoginId(resultSet.getString("LOGIN_ID"));
			student.setName(resultSet.getString("NAME"));
			student.setRole(resultSet.getString("ROLE"));
			student.setPasswordInitialized(resultSet.getBoolean("PASSWORD_INITIALIZED"));
			student.setBirthday(resultSet.getObject("BIRTHDAY", LocalDate.class));
			student.setSchool(school);
			// クラスに在籍していない日はnullのまま
			if (resultSet.getObject("CLASS_ID") != null) {
				SchoolClass schoolClass = new SchoolClass();
				schoolClass.setId(resultSet.getInt("CLASS_ID"));
				schoolClass.setName(resultSet.getString("CLASS_NAME"));
				schoolClass.setSchool(school);
				student.setSchoolClass(schoolClass);
			}
			list.add(student);
		}
		return list;
	}

	/**
	 * getメソッド 学生を1件取得する
	 *
	 * @param id:int 学生のユーザーID
	 * @param school:School 教職員の学校
	 * @return 学生 存在しない・ほかの学校の場合はnull
	 * @throws Exception
	 */
	public Student get(int id, School school) throws Exception {
		List<Student> list;
		LocalDate today = LocalDate.now();

		try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(baseSql + "and S.USER_ID = ?")) {
			statement.setObject(1, today);
			statement.setObject(2, today);
			statement.setInt(3, school.getId());
			statement.setInt(4, id);
			list = postFilter(statement.executeQuery(), school);
		}

		return list.isEmpty() ? null : list.get(0);
	}

	/**
	 * filterメソッド 学校の学生を全員取得する
	 *
	 * @param school:School 学校
	 * @param date:LocalDate この日に在籍していたクラスをセットする
	 * @return 学生のリスト:List<Student>
	 * @throws Exception
	 */
	public List<Student> filter(School school, LocalDate date) throws Exception {
		List<Student> list;

		try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(baseSql + "order by U.LOGIN_ID")) {
			statement.setObject(1, date);
			statement.setObject(2, date);
			statement.setInt(3, school.getId());
			list = postFilter(statement.executeQuery(), school);
		}

		return list;
	}

	/**
	 * filterメソッド 指定した日にそのクラスに在籍していた学生を取得する
	 *
	 * @param school:School 学校
	 * @param schoolClass:SchoolClass クラス
	 * @param date:LocalDate 在籍を調べる日
	 * @return 学生のリスト:List<Student>
	 * @throws Exception
	 */
	public List<Student> filter(School school, SchoolClass schoolClass, LocalDate date) throws Exception {
		List<Student> list;

		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(
					 baseSql + "and CL.ID = ? order by U.LOGIN_ID")
			 )
		{
			statement.setObject(1, date);
			statement.setObject(2, date);
			statement.setInt(3, school.getId());
			statement.setInt(4, schoolClass.getId());
			list = postFilter(statement.executeQuery(), school);
		}

		return list;
	}

	/**
	 * findByLoginIdsメソッド 学生番号から、この学校の学生を取得する（CSVの取り込みで、更新の対象を調べる）
	 * ほかの学校の学生や、教職員のアカウントは含まれない
	 *
	 * @param school:School 教職員の学校
	 * @param loginIds:List<String> 調べたい学生番号
	 * @return 学生番号 → 学生（今日のクラスつき）:Map<String, Student>
	 * @throws Exception
	 */
	public Map<String, Student> findByLoginIds(School school, List<String> loginIds) throws Exception {
		Map<String, Student> map = new HashMap<>();
		if (loginIds.isEmpty()) {
			return map;
		}

		// 「?, ?, ?」を件数分つくる
		String marks = String.join(", ", Collections.nCopies(loginIds.size(), "?"));
		LocalDate today = LocalDate.now();

		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(
					 baseSql + "and U.LOGIN_ID in (" + marks + ")"))
		{
			statement.setObject(1, today);
			statement.setObject(2, today);
			statement.setInt(3, school.getId());
			int index = 4;
			for (String loginId : loginIds) {
				statement.setString(index++, loginId);
			}
			for (Student student : postFilter(statement.executeQuery(), school)) {
				map.put(student.getLoginId(), student);
			}
		}

		return map;
	}

	/**
	 * resetPasswordメソッド 学生のパスワードを生年月日（yyyyMMdd）に戻す
	 *
	 * USERS の PASSWORD と PASSWORD_INITIALIZED だけを更新する。
	 * 勤務先・シフトテンプレート・シフト・報告書など、ほかのデータは変えない
	 * 学校が同じ学生かどうかは、呼ぶ側が get(id, school) で確かめておく
	 *
	 * @param student:Student 学生（生年月日が必要）
	 * @return 成功:true, 失敗（生年月日がない・学生がいない）:false
	 * @throws Exception
	 */
	public boolean resetPassword(Student student) throws Exception {
		if (student == null || student.getBirthday() == null) {
			return false;
		}
		int count = 0;

		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(
					 "update USERS set PASSWORD = ?, PASSWORD_INITIALIZED = false where ID = ? and ROLE = ?"
			 ))
		{
			statement.setString(1, PasswordUtil.hash(student.getBirthday().format(DateTimeFormatter.BASIC_ISO_DATE)));
			statement.setInt(2, student.getId());
			statement.setString(3, User.ROLE_STUDENT);
			count = statement.executeUpdate();
		}

		return count > 0;
	}

	/**
	 * saveAllメソッド CSVの行から、学生アカウントをまとめて登録・更新する
	 *
	 * 行の種類（row.getKind()）で処理を分ける
	 *   新規     ：USERS・STUDENT・CLASS_ENROLLMENT に登録する（初期パスワードは生年月日 yyyyMMdd のハッシュ値）
	 *   更新     ：同じ学生（USERS.ID）の氏名・生年月日を更新し、クラスが変わるときは在籍の履歴を足す
	 *              勤務先・シフト・報告書は学生IDにひもづいているので、そのまま残る。パスワードは変えない
	 *   変更なし ：何もしない
	 * 1つのトランザクションで行い、1件でも失敗したら全部取り消す
	 *
	 * @param rows:List<ImportRow> CSVの行（種類が決まっていること）
	 * @param school:School 教職員の学校
	 * @return {新規の人数, 更新した人数}:int[]
	 * @throws Exception
	 */
	public int[] saveAll(List<ImportRow> rows, School school) throws Exception {
		int created = 0;
		int updated = 0;
		LocalDate today = LocalDate.now();

		try (Connection connection = getConnection()) {
			// 自動コミットを止めて、最後にまとめてコミットする
			connection.setAutoCommit(false);
			try (PreparedStatement classStatement = connection.prepareStatement(
					"select ID from SCHOOL_CLASS where SCHOOL_ID = ? and NAME = ?"
				 );
				 PreparedStatement userStatement = connection.prepareStatement(
					"insert into USERS (LOGIN_ID, PASSWORD, NAME, ROLE, PASSWORD_INITIALIZED) values (?, ?, ?, ?, false)",
						Statement.RETURN_GENERATED_KEYS
				 );
				 PreparedStatement studentStatement = connection.prepareStatement(
					"insert into STUDENT (USER_ID, SCHOOL_ID, BIRTHDAY) values (?, ?, ?)"
				 );
				 PreparedStatement enrollmentStatement = connection.prepareStatement(
					"insert into CLASS_ENROLLMENT (STUDENT_ID, SCHOOL_CLASS_ID, START_DATE) values (?, ?, ?)"
				 );
				 // ---- 更新用 ----
				 PreparedStatement updateUserStatement = connection.prepareStatement(
					"update USERS set NAME = ? where ID = ? and ROLE = ?"
				 );
				 PreparedStatement updateStudentStatement = connection.prepareStatement(
					"update STUDENT set BIRTHDAY = ? where USER_ID = ? and SCHOOL_ID = ?"
				 );
				 PreparedStatement currentStatement = connection.prepareStatement(
					"select ID, SCHOOL_CLASS_ID, START_DATE from CLASS_ENROLLMENT where STUDENT_ID = ? and END_DATE is null"
				 );
				 PreparedStatement moveStatement = connection.prepareStatement(
					"update CLASS_ENROLLMENT set SCHOOL_CLASS_ID = ? where ID = ?"
				 );
				 PreparedStatement closeStatement = connection.prepareStatement(
					"update CLASS_ENROLLMENT set END_DATE = ? where ID = ?"
				 ))
			{
				for (ImportRow row : rows) {
					// エラーのある行・変更のない行は処理しない
					if (row.getError() != null || ImportRow.KIND_UNCHANGED.equals(row.getKind())) {
						continue;
					}

					// クラス名からクラスIDを取得
					classStatement.setInt(1, school.getId());
					classStatement.setString(2, row.getClassName());
					ResultSet classResult = classStatement.executeQuery();
					if (!classResult.next()) {
						throw new Exception(row.getLineNo() + "行目：クラス「" + row.getClassName() + "」がありません");
					}
					int classId = classResult.getInt("ID");

					if (ImportRow.KIND_UPDATE.equals(row.getKind())) {
						// ---- 更新：同じ学生の情報を書き換える（IDは変えないので、シフトなどの記録は残る） ----

						// 生年月日（この学校の学生でなければ0件になるので、ほかの学校の学生は書き換えられない）
						updateStudentStatement.setObject(1, row.getBirthday());
						updateStudentStatement.setInt(2, row.getUserId());
						updateStudentStatement.setInt(3, school.getId());
						if (updateStudentStatement.executeUpdate() == 0) {
							throw new Exception(row.getLineNo() + "行目：この学校の学生ではありません");
						}

						// 氏名
						updateUserStatement.setString(1, row.getName());
						updateUserStatement.setInt(2, row.getUserId());
						updateUserStatement.setString(3, User.ROLE_STUDENT);
						if (updateUserStatement.executeUpdate() == 0) {
							throw new Exception(row.getLineNo() + "行目：学生が見つかりません");
						}

						// クラス（変わるときだけ）
						currentStatement.setInt(1, row.getUserId());
						ResultSet current = currentStatement.executeQuery();
						if (current.next()) {
							int enrollmentId = current.getInt("ID");
							int currentClassId = current.getInt("SCHOOL_CLASS_ID");
							LocalDate start = current.getObject("START_DATE", LocalDate.class);
							if (currentClassId != classId) {
								if (!start.isBefore(today)) {
									// 今日から始まった在籍は、履歴を足さずにクラスだけ直す（終了日が開始日より前になるのを避ける）
									moveStatement.setInt(1, classId);
									moveStatement.setInt(2, enrollmentId);
									moveStatement.executeUpdate();
								} else {
									// いまの在籍を前日で閉じて、新しいクラスの在籍を今日から足す（クラス替えの履歴が残る）
									closeStatement.setObject(1, today.minusDays(1));
									closeStatement.setInt(2, enrollmentId);
									closeStatement.executeUpdate();
									enrollmentStatement.setInt(1, row.getUserId());
									enrollmentStatement.setInt(2, classId);
									enrollmentStatement.setObject(3, today);
									enrollmentStatement.executeUpdate();
								}
							}
						} else {
							// どのクラスにも在籍していない学生は、新しく在籍を足す
							enrollmentStatement.setInt(1, row.getUserId());
							enrollmentStatement.setInt(2, classId);
							enrollmentStatement.setObject(3, today);
							enrollmentStatement.executeUpdate();
						}

						updated++;
					} else {
						// ---- 新規：これまでどおり登録する ----

						// ユーザーを登録し、自動で振られたIDを受け取る
						userStatement.setString(1, row.getStudentNo());
						userStatement.setString(2, PasswordUtil.hash(row.getBirthday().format(DateTimeFormatter.BASIC_ISO_DATE)));
						userStatement.setString(3, row.getName());
						userStatement.setString(4, User.ROLE_STUDENT);
						userStatement.executeUpdate();
						ResultSet keys = userStatement.getGeneratedKeys();
						keys.next();
						int userId = keys.getInt(1);

						// 学生とクラス在籍を登録
						studentStatement.setInt(1, userId);
						studentStatement.setInt(2, school.getId());
						studentStatement.setObject(3, row.getBirthday());
						studentStatement.executeUpdate();

						enrollmentStatement.setInt(1, userId);
						enrollmentStatement.setInt(2, classId);
						enrollmentStatement.setObject(3, today);
						enrollmentStatement.executeUpdate();

						created++;
					}
				}
				// 全部成功したらコミット
				connection.commit();
			} catch (Exception e) {
				// 失敗したら全部取り消す
				connection.rollback();
				throw e;
			}
		}

		return new int[] { created, updated };
	}
}