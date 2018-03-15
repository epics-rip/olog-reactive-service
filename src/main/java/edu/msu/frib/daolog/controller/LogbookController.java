package edu.msu.frib.daolog.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.msu.frib.daolog.log.Logbook;
import edu.msu.frib.daolog.log.LogbookWrapper;
import edu.msu.frib.daolog.repository.LogbookDBUtils;
import edu.msu.frib.daolog.repository.LogbookRepository;

@Controller
public class LogbookController {

	private static Logger logger = LoggerFactory.getLogger(LogbookController.class);

	@Autowired
	private LogbookRepository logbookRepository;
	


	@PostMapping("/daolog/resources/logbook/createByJSON")
	@ResponseBody
	public Logbook testCreateLogbookJSON(@RequestBody Logbook logbook) {
		logger.info("creating new logbook " + logbook.getName());

		logbook.setCreatedDate(new Date());
		Logbook savedLogbook = logbookRepository.save(logbook);
		logger.info("new logbook: " + logbook);

		return savedLogbook;
	}

	@PostMapping("/daolog/resources/logbooks")
	@ResponseBody
	public Logbook createLogbookJSON(@RequestBody LogbookWrapper logbk) {
		
		Logbook savedLogbook = null;
		
		for (Logbook logbook : logbk.getLogbook()) {
			logbook.setCreatedDate(new Date());			
			savedLogbook = logbookRepository.save(logbook);	
			logger.info("new logbook: " + logbook);
	
		}

		return savedLogbook;
	}
	
	
//	@PostMapping("/daolog/resources/logbooks")
//	@ResponseBody
//	public Logbook createLogbookJSON(@RequestParam String name, @RequestParam String owner) {
//		logger.info("creating new logbook {}:{}", name, owner);
//		
//		Logbook logbook = new Logbook(name, owner, State.Active);
//		logbook.setCreatedDate(new Date());
//		
//		Logbook savedLogbook = logbookRepository.save(logbook);
//
//		logger.info("new logbook: " + logbook);
//
//		return savedLogbook;
//	}

	@PostMapping("/daolog/resources/logbooks/createByJSON")
	@ResponseBody
	public Set<Logbook> createLogbooksByJSON(InputStream logbooksStream) throws JsonParseException, JsonMappingException, IOException {
		
		// call MongoDB to insert them all into the database
		Set<Logbook> savedLogbooks = LogbookDBUtils.insertLogbooks(logbookRepository, logbooksStream);		
		return savedLogbooks; 
	}
	
	/**
	 * GET logbooks
	 * HTML UI expects a single object named logbook with an array of Logbook objects
	 * The wrapper provides jackson with the structure to create what the UI needs
	 * @return
	 */
	@GetMapping("/daolog/resources/logbooks")
	@ResponseBody
	public LogbookWrapper getLogbooks() {
		logger.info("findAll() logbooks from mongodb!");
		List<Logbook> logbooks = logbookRepository.findAll();
		LogbookWrapper wrapper = new LogbookWrapper();
		
		wrapper.setLogbook(logbooks);
		return wrapper;
	}
	

	@DeleteMapping("/daolog/delete-logbooks")
	@ResponseBody
	public void deleteAllLogbooks() throws IOException {

		logger.info("delete all logbooks documents from mongodb");
		logbookRepository.deleteAll();

	}
}
