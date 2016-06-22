package com.hssc.ems.pojo;

import java.util.Date;

import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("com_door_record")
public class DoorRecord {
	@Id
	int id;
	String card;//卡号
	Date time;//开门时间
	int number;//机号
	String volidate;//验证方式
	String remark;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCard() {
		return card;
	}
	public void setCard(String card) {
		this.card = card;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public String getVolidate() {
		return volidate;
	}
	public void setVolidate(String volidate) {
		this.volidate = volidate;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}

