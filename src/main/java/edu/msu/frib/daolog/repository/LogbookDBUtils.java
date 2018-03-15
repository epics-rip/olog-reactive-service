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

import edu.msu.frib.daolog.log.Logbook;
import edu.msu.frib.daolog.log.LogbooksWrapper;

public class LogbookDBUtils {
	private static Logger logger = LoggerFactory.getLogger(LogbookDBUtils.class);
	
	public static Logbook findLogbook(LogbookRepository logbookRepository, String logbookId) {

        Logbook logbook = logbookRepository.findById(logbookId);        
    	logging(logbook, "findLogbooks(LogbookRepository logbookRepository): ");
    	return logbook;
	}
	
	public static Set<Logbook> findLogbooks(LogbookRepository logbookRepository) {

        List<Logbook> listLogs = logbookRepository.findAll();
        
        Set<Logbook> logbooks = new HashSet<Logbook>();
        listLogs.forEach(logbook -> logbooks.add(logbook));

    	logging(logbooks, "findLogbooks(LogbookRepository logbookRepository): ");
    	return logbooks;
	}
	
	// Method to transform sparse Logbook objects with just _ids to full-fledged Logbook objects
	public static Set<Logbook> findLogbooks(LogbookRepository logbookRepository, Set<Logbook> sparseLogbooks) {
		
        List<String> logbookIds = new LinkedList<String>();        
        Set<Logbook> logbooks = new HashSet<Logbook>();
        
        sparseLogbooks.forEach(logbook -> logbookIds.add(logbook.getId()));

    	// Perform the query to return the Iterable result of all matching Logbook objects
    	// then migrates the objects into the HashSet to return
    	(logbookRepository.findAll(logbookIds)).forEach(log -> logbooks.add(log));

    	logging(logbooks, "findLogbooks(LogbookRepository logbookRepository, Set<Logbook> sparseLogbooks) ");
        return logbooks;
	}
	
	// Method to transform list of Logbook ids full-fledged Logbook objects
	public static Set<Logbook> findLogbooks(LogbookRepository logbookRepository, List<String> logbookIds) {
	      
        Set<Logbook> logbooks = new HashSet<Logbook>();
        
    	// Perform the query to return the Iterable result of all matching Logbook objects
    	// then migrates the objects into the HashSet to return
    	(logbookRepository.findAll(logbookIds)).forEach(Logbook -> logbooks.add(Logbook));
    	
    	logging(logbooks, "findLogbooks(LogbookRepository logbookRepository, List<String> logbookIds) ");
        
        return logbooks;
	}
	
	public static Set<Logbook> insertLogbooks(LogbookRepository logbookRepository, InputStream logbooksStream) 
			throws JsonParseException, JsonMappingException, IOException {

		if (logbookRepository == null) {
			throw new IllegalArgumentException("logbookRepository may not be null");
		}

		if (logbooksStream == null) {
			throw new IllegalArgumentException("logbooksStream may not be null");
		}
		
		ObjectMapper mapper = new ObjectMapper();
		LogbooksWrapper logbooksWrapper = new LogbooksWrapper();
			
		// convert into a LogbooksWrapper object
		logbooksWrapper = mapper.readValue(logbooksStream, LogbooksWrapper.class);
		
        List<Logbook> logbooksList = logbookRepository.insert(logbooksWrapper.getLogbooks());
        Set<Logbook> logbooks = new HashSet<Logbook>(logbooksList);
		return logbooks;
	}
	
	public static Set<Logbook> insertLogbooks(MongoTemplate mongoTemplate, InputStream logbooksStream) 
			throws JsonParseException, JsonMappingException, IOException {
		
		ObjectMapper mapper = new ObjectMapper();

		LogbooksWrapper logbooksWrapper = new LogbooksWrapper();
			
		// convert into a LogbooksWrapper object
		logbooksWrapper = mapper.readValue(logbooksStream, LogbooksWrapper.class);	

		if (mongoTemplate == null) {
			logger.info("mongoTemplate is null!");
		}
		if (logbooksWrapper == null) {
			logger.info("logbooksWrapper is null!");
		}
		
        mongoTemplate.insertAll(logbooksWrapper.getLogbooks());        
        List<Logbook> logbooksList = mongoTemplate.findAll(Logbook.class);

        Set<Logbook> logbooksSet = new HashSet<Logbook>(logbooksList);
        
        return logbooksSet;
	}
	
	public static void logging(Logbook logbook, String info) {
		Set<Logbook> logbooks = new HashSet<Logbook>();
		logbooks.add(logbook);
		logging(logbooks, info);
	}
	
	public static void logging(Set<Logbook> logbooks, String info) {

        StringBuffer buff = new StringBuffer();
        buff.append(info);
        buff.append("Logbook count: " + logbooks.size() + " ");
        
        for (Logbook logbook : logbooks) {
        	buff.append("\nLogbook: " + logbook.toString());
        }

        logger.info(buff.toString());
	}
}
