package bean;

import java.io.Serializable;

/**
 * 学校
 */
public class School implements Serializable {

	/**
	 * ID:int
	 */
	private int id;

	/**
	 * 学校コード:String
	 */
	private String code;

	/**
	 * 学校名:String
	 */
	private String name;

	/**
	 * 通常の7日間上限（28）:double
	 */
	private double normalLimitHours;

	/**
	 * ゲッター、セッター
	 */
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getNormalLimitHours() {
		return normalLimitHours;
	}

	public void setNormalLimitHours(double normalLimitHours) {
		this.normalLimitHours = normalLimitHours;
	}
}
