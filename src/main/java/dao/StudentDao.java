package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import bean.ImportRow;
import bean.School;
import bean.SchoolClass;
import bean.Student;
import bean.User;

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
			  "select U.ID, U.LOGIN_ID, U.NAME, U.ROLE, U.PASSWORD_INITIALIZED, "
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
	 * saveAllメソッド CSVの行から学生アカウントをまとめて作る
	 * 
	 * USERS・STUDENT・CLASS_ENROLLMENT を1つのトランザクションで登録し、
	 * 1件でも失敗したら全部取り消す
	 *
	 * @param rows:List<ImportRow> CSVの行
	 * @param school:School 教職員の学校
	 * @param passwordHash:String 初期パスワードのハッシュ値
	 * @return 作成した人数:int
	 * @throws Exception
	 */
	public int saveAll(List<ImportRow> rows, School school, String passwordHash) throws Exception {
		int count = 0;

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
					"insert into STUDENT (USER_ID, SCHOOL_ID) values (?, ?)"
				 );
				 PreparedStatement enrollmentStatement = connection.prepareStatement(
					"insert into CLASS_ENROLLMENT (STUDENT_ID, SCHOOL_CLASS_ID, START_DATE) values (?, ?, ?)"
				 ))
			{
				for (ImportRow row : rows) {
					// エラーのある行は登録しない
					if (row.getError() != null) {
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

					// ユーザーを登録し、自動で振られたIDを受け取る
					userStatement.setString(1, row.getStudentNo());
					userStatement.setString(2, passwordHash);
					userStatement.setString(3, row.getName());
					userStatement.setString(4, User.ROLE_STUDENT);
					userStatement.executeUpdate();
					ResultSet keys = userStatement.getGeneratedKeys();
					keys.next();
					int userId = keys.getInt(1);

					// 学生とクラス在籍を登録
					studentStatement.setInt(1, userId);
					studentStatement.setInt(2, school.getId());
					studentStatement.executeUpdate();

					enrollmentStatement.setInt(1, userId);
					enrollmentStatement.setInt(2, classId);
					enrollmentStatement.setObject(3, LocalDate.now());
					enrollmentStatement.executeUpdate();

					count++;
				}
				// 全部成功したらコミット
				connection.commit();
			} catch (Exception e) {
				// 失敗したら全部取り消す
				connection.rollback();
				throw e;
			}
		}

		return count;
	}
}
