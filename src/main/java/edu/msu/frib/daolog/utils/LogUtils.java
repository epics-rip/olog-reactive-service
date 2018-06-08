package edu.msu.frib.daolog.utils;

import edu.msu.frib.daolog.log.Log;

public class LogUtils {

	public static Log updateLogEntry(Log newLog, Log originalLog) {
		
		if (newLog == null) {
			throw new IllegalArgumentException("new log may not be null");
		}
		if (originalLog == null) {
			throw new IllegalArgumentException("original log may not be null");
		}
				
		// set the entry value to the parent OR the existing entry value (if defined---a grandchild object, if you will)
		newLog.setEntry( (originalLog.getEntry() == null) ? originalLog.getId() : originalLog.getEntry() );
				
		return newLog;
	}

	public static Log updateLogVersion(Log newLog, Log originalLog) {

		if (newLog == null) {
			throw new IllegalArgumentException("new log may not be null");
		}
		if (originalLog == null) {
			throw new IllegalArgumentException("original log may not be null");
		}
		// increment the version number
		newLog.setVersion(originalLog.getVersion() + 1);
		return newLog;
	}
}
