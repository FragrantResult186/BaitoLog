package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bean.School;
import bean.SchoolClass;
import bean.Student;
import bean.Teacher;
import bean.User;
import logic.PasswordUtil;

/**
 * ユーザー（USERS）を扱うDao
 */
public class UserDao extends Dao {

	/**
	 * ログイン用のSQL
	 */
	private String loginSql =
			  "select U.*, "
			+ "SC.ID as SCHOOL_ID, SC.CODE as SCHOOL_CODE, SC.NAME as SCHOOL_NAME, SC.NORMAL_LIMIT_HOURS, "
			+ "CL.ID as CLASS_ID, CL.NAME as CLASS_NAME "
			+ "from USERS U "
			+ "left join STUDENT S on S.USER_ID = U.ID "
			+ "left join TEACHER T on T.USER_ID = U.ID "
			+ "left join SCHOOL SC on SC.ID = coalesce(S.SCHOOL_ID, T.SCHOOL_ID) "
			+ "left join CLASS_ENROLLMENT CE on CE.STUDENT_ID = S.USER_ID and CE.END_DATE is null "
			+ "left join SCHOOL_CLASS CL on CL.ID = CE.SCHOOL_CLASS_ID "
			+ "where U.LOGIN_ID = ? and U.PASSWORD = ? and U.IS_ACTIVE = true";

	/**
	 * loginメソッド ログインIDとパスワードで認証する
	 *
	 * @param loginId:String ログインID
	 * @param password:String 入力されたパスワード
	 * @return 認証成功:Student または Teacher のインスタンス、認証失敗:null
	 * @throws Exception
	 */
	public User login(String loginId, String password) throws Exception {
		// ユーザーを初期化
		User user = null;

		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(loginSql))
		{
			// ログインIDとハッシュ化したパスワードをバインド
			statement.setString(1, loginId);
			statement.setString(2, PasswordUtil.hash(password));
			ResultSet resultSet = statement.executeQuery();

			if (resultSet.next()) {
				// 学校をセット
				School school = new School();
				school.setId(resultSet.getInt("SCHOOL_ID"));
				school.setCode(resultSet.getString("SCHOOL_CODE"));
				school.setName(resultSet.getString("SCHOOL_NAME"));
				school.setNormalLimitHours(resultSet.getDouble("NORMAL_LIMIT_HOURS"));

				if (User.ROLE_STUDENT.equals(resultSet.getString("ROLE"))) {
					// 学生の場合は今のクラスもセット
					Student student = new Student();
					student.setSchool(school);
					if (resultSet.getObject("CLASS_ID") != null) {
						SchoolClass schoolClass = new SchoolClass();
						schoolClass.setId(resultSet.getInt("CLASS_ID"));
						schoolClass.setName(resultSet.getString("CLASS_NAME"));
						schoolClass.setSchool(school);
						student.setSchoolClass(schoolClass);
					}
					user = student;
				} else {
					// 教職員の場合
					Teacher teacher = new Teacher();
					teacher.setSchool(school);
					user = teacher;
				}

				// 共通の項目をセット
				user.setId(resultSet.getInt("ID"));
				user.setLoginId(resultSet.getString("LOGIN_ID"));
				user.setName(resultSet.getString("NAME"));
				user.setRole(resultSet.getString("ROLE"));
				user.setPasswordInitialized(resultSet.getBoolean("PASSWORD_INITIALIZED"));
				user.setAuthenticated(true);
			}
		}

		return user;
	}

	/**
	 * updatePasswordメソッド パスワードを変更し、初回パスワード登録済みにする
	 *
	 * @param user:User ログイン中のユーザー
	 * @param newPassword:String 新しいパスワード
	 * @return 成功:true, 失敗:false
	 * @throws Exception
	 */
	public boolean updatePassword(User user, String newPassword) throws Exception {
		// 更新した件数
		int count = 0;

		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(
					 "update USERS set PASSWORD = ?, PASSWORD_INITIALIZED = true where ID = ?"
			 ))
		{
			statement.setString(1, PasswordUtil.hash(newPassword));
			statement.setInt(2, user.getId());
			count = statement.executeUpdate();
		}

		return count > 0; // 更新した1件以上あるか
	}

	/**
	 * filterLoginIdsメソッド 指定したログインIDのうち、登録済みのものだけを返す
	 *
	 * @param loginIds:List<String> 調べたいログインID
	 * @return 登録済みのログインID:List<String>
	 * @throws Exception
	 */
	public List<String> filterLoginIds(List<String> loginIds) throws Exception {
		// 結果のリスト
		List<String> list = new ArrayList<>();
		if (loginIds.isEmpty()) {
			return list;
		}

		// 「?, ?, ?」を件数分つくる
		String marks = String.join(", ", Collections.nCopies(loginIds.size(), "?"));

		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(
					 "select LOGIN_ID from USERS where LOGIN_ID in (" + marks + ")"
			 ))
		{
			for (int i = 0; i < loginIds.size(); i++) {
				statement.setString(i + 1, loginIds.get(i));
			}
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				list.add(resultSet.getString("LOGIN_ID"));
			}
		}

		return list;
	}
}
