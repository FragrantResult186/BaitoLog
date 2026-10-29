package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import bean.School;
import bean.SchoolClass;

/**
 * クラス（SCHOOL_CLASS）を扱うDao
 */
public class SchoolClassDao extends Dao {

	/**
	 * filterメソッド 学校のクラスを全部取得する（セレクトボックス用）
	 *
	 * @param school:School
	 *            学校
	 * @return クラスのリスト（クラス名順）:List<SchoolClass>
	 * @throws Exception
	 */
	public List<SchoolClass> filter(School school) throws Exception {
		List<SchoolClass> list = new ArrayList<>();

		try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"select * from SCHOOL_CLASS where SCHOOL_ID = ? order by NAME")) {
			statement.setInt(1, school.getId());
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				SchoolClass schoolClass = new SchoolClass();
				schoolClass.setId(resultSet.getInt("ID"));
				schoolClass.setName(resultSet.getString("NAME"));
				schoolClass.setSchool(school);
				list.add(schoolClass);
			}
		}

		return list;
	}

	/**
	 * getメソッド クラス名を指定してクラスを1件取得する
	 *
	 * @param name:String
	 *            クラス名
	 * @param school:School
	 *            学校
	 * @return クラス 存在しない場合はnull
	 * @throws Exception
	 */
	public SchoolClass get(String name, School school) throws Exception {
		SchoolClass schoolClass = null;

		try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"select * from SCHOOL_CLASS where SCHOOL_ID = ? and NAME = ?")) {
			statement.setInt(1, school.getId());
			statement.setString(2, name);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				schoolClass = new SchoolClass();
				schoolClass.setId(resultSet.getInt("ID"));
				schoolClass.setName(resultSet.getString("NAME"));
				schoolClass.setSchool(school);
			}
		}

		return schoolClass;
	}
}
