package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import bean.LongBreak;
import bean.School;

public class LongBreakDao extends Dao {
	public List<LongBreak> filter(School school, LocalDate from, LocalDate to) throws Exception {
		List<LongBreak> list = new ArrayList<>();
		
		try (Connection connection = getConnection();
		 PreparedStatement statement = connection.prepareStatement(
				 ""
			))
		
		{
			statement.setInt(1, school.getId());
			statement.setObject(2, to);
			statement.setObject(3, from);
			ResultSet resultSet = statement.executeQuery();
			
			while (resultSet.next()) {
				LongBreak longBreak = new LongBreak();
				longBreak.setId(resultSet.getInt("ID"));
				longBreak.setSchool(school);
				longBreak.setStartDate(resultSet.getObject("START_DATE", LocalDate.class));
				longBreak.setEndDate(resultSet.getObject("END_DATE", LocalDate.class));
				longBreak.setLimitHours(resultSet.getDouble("LIMIT_HOURS"));
				list.add(longBreak);
			}
		}
		
		return list;
	}
}


