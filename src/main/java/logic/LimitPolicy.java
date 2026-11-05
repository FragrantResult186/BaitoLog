package logic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import bean.LongBreak;
import bean.School;

public class LimitPolicy {

	private double normalLimitHours;

	private List<LongBreak> longBreaks;

	public LimitPolicy(School school, List<LongBreak> longBreaks) {
		this.normalLimitHours = school.getNormalLimitHours();
		this.longBreaks = (longBreaks == null) ? new ArrayList<>() : longBreaks;
	}

	public double getLimitHours(LocalDate date) {
		double limit = -1;
		for (LongBreak longBreak : longBreaks) {
			boolean inBreak = !date.isBefore(longBreak.getStartDate())
					&& !date.isAfter(longBreak.getEndDate());
			if (inBreak) {
				limit = Math.max(limit, longBreak.getLimitHours());
			}
		}
		return (limit < 0) ? normalLimitHours : limit;
	}

	public double getLimitHours(LocalDate from, LocalDate to) {
		double limit = getLimitHours(from);
		for (LocalDate date = from.plusDays(1); !date.isAfter(to); date = date.plusDays(1)) {
			limit = Math.min(limit, getLimitHours(date));
		}
		return limit;
	}
}