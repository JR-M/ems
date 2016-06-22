package com.hssc.ems.interchange;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator 发送命令
 */
public class BinaryCommand {

	int sensorId;

	String ip;

	int port;

	String commandName;

	String[] range;

	List<byte[]> ask;
	
	List<byte[]> rep;

	String[] feedback;

	ArrayList<BinaryCommand> binaryCommands;
	
	public ArrayList<BinaryCommand> getBinaryCommands() {
		return binaryCommands;
	}

	public void setBinaryCommands(ArrayList<BinaryCommand> bcs) {
		this.binaryCommands = bcs;
	}

	public int getSensorId() {
		return sensorId;
	}

	public void setSensorId(int sensorId) {
		this.sensorId = sensorId;
	}

	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String[] getRange() {
		return range;
	}

	public void setRange(String[] range) {
		this.range = range;
	}

	public List<byte[]> getAsk() {
		return ask;
	}

	public void setAsk(List<byte[]> ask) {
		this.ask = ask;
	}

	
	
	public List<byte[]> getRep() {
		return rep;
	}

	public void setRep(List<byte[]> rep) {
		this.rep = rep;
	}

	public String[] getFeedback() {
		return feedback;
	}

	public void setFeedback(String[] feedback) {
		this.feedback = feedback;
	}

}
