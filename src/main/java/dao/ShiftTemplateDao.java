package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import bean.ShiftTemplate;

/**
 * シフトテンプレートDAO（テーブル SHIFT_TEMPLATE）
 * テンプレートの登録・削除は、勤務先と一緒に WorkplaceDao で行う
 */
public class ShiftTemplateDao extends Dao {

	/**
	 * findByWorkplaceメソッド 勤務先に紐づくテンプレートを取得する
	 *
	 * @param workplaceId:int 勤務先ID
	 * @return テンプレートのリスト:List<ShiftTemplate>（開始時刻の早い順）
	 * @throws Exception
	 */
	public List<ShiftTemplate> findByWorkplace(int workplaceId) throws Exception {
		List<ShiftTemplate> list = new ArrayList<>();

		try (Connection connection = getConnection();
				PreparedStatement statement = connection.prepareStatement(
						"select id, workplace_id, name, start_time, end_time, break_min "
								+ "from shift_template where workplace_id = ? order by start_time, id")) {
			statement.setInt(1, workplaceId);
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					ShiftTemplate template = new ShiftTemplate();
					template.setId(resultSet.getInt("id"));
					template.setWorkplaceId(resultSet.getInt("workplace_id"));
					template.setName(resultSet.getString("name"));
					// 時刻は「HH:mm」の文字列にする
					template.setStartTime(resultSet.getTime("start_time").toLocalTime().toString());
					template.setEndTime(resultSet.getTime("end_time").toLocalTime().toString());
					template.setBreakMin(resultSet.getInt("break_min"));
					list.add(template);
				}
			}
		}
		return list;
	}
}
