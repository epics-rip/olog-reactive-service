package edu.msu.frib.daolog.log;

import java.util.ArrayList;
import java.util.List;

public class LogWrapper {

	private List<Log> log = new ArrayList<Log>();

	public List<Log> getLog() {
		return log;
	}

	public void setLog(List<Log> log) {
		this.log = log;
	}
	
}
