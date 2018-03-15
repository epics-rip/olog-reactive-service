package edu.msu.frib.daolog.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.msu.frib.daolog.log.Log;
import edu.msu.frib.daolog.log.LogsWrapper;

public class LogDBUtils {

	private static Logger logger = LoggerFactory.getLogger(LogDBUtils.class);
	

	public static Log findLog(LogRepository logRepository, String logId) {

        Log log = logRepository.findById(logId);        
    	logging(log, "findAllLogs(logRepository logRepository): ");
    	return log;
	}
	
	public static Set<Log> findLogs(LogRepository logRepository) {

        List<Log> listLogs = logRepository.findAll();
        
        Set<Log> logs = new HashSet<Log>();
        listLogs.forEach(log -> logs.add(log));

    	logging(logs, "findAllLogs(LogRepository logRepository): ");
    	return logs;
	}
	
	// Method to transform sparse Log objects with just _ids to full-fledged Log objects
	public static Set<Log> findLogs(LogRepository logRepository, Set<Log> logs) {
		
        List<String> log_ids = new LinkedList<String>();        
        Set<Log> logsProper = new HashSet<Log>();
        
        logs.forEach(Log -> log_ids.add(Log.getId()));

    	// Perform the query to return the Iterable result of all matching Log objects
    	// then migrates the objects into the HashSet to return
    	(logRepository.findAll(log_ids)).forEach(log -> logsProper.add(log));

    	logging(logs, "findLogs(LogRepository logRepository, Set<Log> logs) ");
        return logsProper;
	}
	
	// Method to transform list of Log ids full-fledged Log objects
	public static Set<Log> findLogs(LogRepository logRepository, List<String> logList) {
	      
        Set<Log> logs = new HashSet<Log>();
        
    	// Perform the query to return the Iterable result of all matching Log objects
    	// then migrates the objects into the HashSet to return
    	(logRepository.findAll(logList)).forEach(Log -> logs.add(Log));
    	
    	logging(logs, "findLogs(LogRepository logRepository, List<String> logList) ");
        
        return logs;
	}
	
	// Method to find Log with greatest version number in an Entry collection
	public static Log findLogWithMaxVersion(LogRepository logRepository, Log log) {
		return logRepository.findFirstByEntryOrderByVersionDesc(log.getEntry());
	}
	
	public static Set<Log> insertLogs(LogRepository logRepository, InputStream logsStream) 
			throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();
		LogsWrapper logsWrapper = new LogsWrapper();
			
		// convert into a TagsWrapper object
		logsWrapper = mapper.readValue(logsStream, LogsWrapper.class);	
		
        List<Log> logsList = logRepository.insert(logsWrapper.getLogs());
        Set<Log> logs = new HashSet<Log>(logsList);
		return logs;
	}	

	public static Set<Log> insertLogs(MongoTemplate mongoTemplate, InputStream logsStream) 
			throws JsonParseException, JsonMappingException, IOException {
		

		if (mongoTemplate == null) {
			logger.info("mongoTemplate is null!");
		}
		if (logsStream == null) {
			logger.info("logsStream is null!");
		}
		
		ObjectMapper mapper = new ObjectMapper();

		LogsWrapper logsWrapper = new LogsWrapper();
			
		// convert into a LogsWrapper object
		logsWrapper = mapper.readValue(logsStream, LogsWrapper.class);	

		if (logsWrapper == null) {
			logger.info("logsWrapper is null!");
		}
		
        mongoTemplate.insertAll(logsWrapper.getLogs());        
        List<Log> logsList = mongoTemplate.findAll(Log.class);

        Set<Log> logsSet = new HashSet<Log>(logsList);
        
        return logsSet;
	}
	
	public static void logging(Log log, String info) {
		Set<Log> logs = new HashSet<Log>();
		logs.add(log);
		logging(logs, info);
	}
	
	public static void logging(Set<Log> logs, String info) {

        StringBuffer buff = new StringBuffer();
        buff.append(info);
        buff.append("Log count: " + logs.size() + " ");
        
        for (Log log : logs) {
        	buff.append("\nLog: " + log.toString());
        }

        logger.info(buff.toString());
	}
}
