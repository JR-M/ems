package com.hssc.ems.pojo;

import java.util.Date;

import org.nutz.dao.entity.annotation.Id;
import org.nutz.dao.entity.annotation.Table;

@Table("com_room_warning")
public class Warning {

	@Id
	int id;

	int sensorId;

	Date warningTime;

	float warningVal;

	String warningType;

	String status;

	Date processTime;
	
	String person;

	String details;

	String remark;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSensorId() {
		return sensorId;
	}

	public void setSensorId(int sensorId) {
		this.sensorId = sensorId;
	}

	public Date getWarningTime() {
		return warningTime;
	}

	public void setWarningTime(Date warningTime) {
		this.warningTime = warningTime;
	}

	public float getWarningVal() {
		return warningVal;
	}

	public void setWarningVal(float f) {
		this.warningVal = f;
	}

	public String getWarningType() {
		return warningType;
	}

	public void setWarningType(String warningType) {
		this.warningType = warningType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getProcessTime() {
		return processTime;
	}

	public void setProcessTime(Date processTime) {
		this.processTime = processTime;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
}
