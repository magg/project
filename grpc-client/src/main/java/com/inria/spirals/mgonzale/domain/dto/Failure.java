package com.inria.spirals.mgonzale.domain.dto;

public class Failure {
	
	private long amount;
	private String path;
	private int periodSec = 0;
	private String host;
	private int port;
	private String cmd;
	
	private String iface;
	
	private String type;
	
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public int getPeriodSec() {
		return periodSec;
	}
	public void setPeriodSec(int periodSec) {
		this.periodSec = periodSec;
	}

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getIface() {
		return iface;
	}
	public void setIface(String iface) {
		this.iface = iface;
	}
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}

}
