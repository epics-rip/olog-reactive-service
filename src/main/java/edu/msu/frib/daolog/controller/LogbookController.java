package edu.msu.frib.daolog.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.msu.frib.daolog.log.Log;
import edu.msu.frib.daolog.log.Logbook;
import edu.msu.frib.daolog.log.LogbookWrapper;
import edu.msu.frib.daolog.repository.LogRepository;
import edu.msu.frib.daolog.repository.LogbookRepository;

@Controller
@RequestMapping("/daolog/resources/logbooks")
public class LogbookController {

	private static Logger logger = LoggerFactory.getLogger(LogbookController.class);

	@Autowired
	private LogbookRepository logbookRepository;
	@Autowired
	private LogRepository logRepository;
	

	/**
	 * Create logbook document in logbooks collection from LogbookWrapper object 
	 * (which wraps arrays of objects like what we receive from the existing frontend UI)
	 * 
	 * Returns JSON object with finalized/saved logbook
	 * 
	 * @param logbk
	 * @return savedLogbook
	 */
	@PostMapping
	@PreAuthorize("hasAuthority('ROLE_OLOG-LOGBOOKS')")
	@ResponseBody
	public LogbookWrapper createLogbook(@RequestBody LogbookWrapper logbk) {
		
		Logbook savedLogbook = null;
		LogbookWrapper savedLogbookWrapper = new LogbookWrapper();
		List<Logbook> savedLogbookList = new ArrayList<Logbook>();
		
		for (Logbook logbook : logbk.getLogbook()) {
			logbook.setCreatedDate(new Date());			
			savedLogbook = logbookRepository.save(logbook);	
			logger.info("new logbook: " + logbook);
			savedLogbookList.add(savedLogbook);
		}

		savedLogbookWrapper.setLogbook(savedLogbookList);
		return savedLogbookWrapper;
	}
	
	
	/**
	 * GET logbooks
	 * HTML UI expects a single object named logbook with an array of Logbook objects
	 * The wrapper provides jackson with the structure to create what the UI needs
	 * @return
	 */
	@GetMapping
	@ResponseBody
	public LogbookWrapper getLogbooks() {
		logger.info("findAll() logbooks from mongodb!");
		List<Logbook> logbooks = logbookRepository.findAll();

		logbooks.forEach(logbook -> logger.info("findAll() logbooks from mongodb! {}", logbook.getName()));
		LogbookWrapper wrapper = new LogbookWrapper();
		
		wrapper.setLogbook(logbooks);
		return wrapper;
	}
	
	// TODO Add 'normal' delete which merely de-activates logbooks, and removes them from visibility through the API
	// TODO refactor: separate controller and database concerns
	

	// TODO this delete APi is incomplete...
	@DeleteMapping("id/{id}")
	@PreAuthorize("hasAuthority('ROLE_OLOG-ADMINS')")
	@ResponseBody
	public ResponseEntity deleteLogbookById(@PathVariable String id) throws IOException {
				
		Optional<Logbook> logbook = logbookRepository.findById(id);
		
		if (logbook == null) {
			logger.debug("logbook {} not found", id);
			return new ResponseEntity<>("logbook id("+id+") not found",HttpStatus.BAD_REQUEST);
			
		} else { 
			Log sparseLog = new Log();
			Logbook exampleLogbook = new Logbook();
			exampleLogbook.setId(id);
			Set<Logbook> exampleLogbooksSet = new HashSet<Logbook>();
			exampleLogbook.setName(logbook.get().getName());
	
			exampleLogbooksSet.add(exampleLogbook);
			sparseLog.setLogbooks(exampleLogbooksSet);
			
			ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("logbooks.name", ExampleMatcher.GenericPropertyMatchers.exact());
			Example<Log> exampleLog = Example.of(sparseLog, matcher);
			
			long logbooksCount = logRepository.count(exampleLog);
			
			if (logbooksCount == 0) {
				logger.info("delete {} logbook document from mongodb", id);
				logbookRepository.delete(exampleLogbook);
				logger.info("logbook {} deleted successfully", id);
			} else {

				logger.debug("logbook {} still in use", id);
				return new ResponseEntity<>("logbook("+id+") still in use", HttpStatus.BAD_REQUEST);
			}
		}
		return new ResponseEntity<>(HttpStatus.OK);

	}
	
	@DeleteMapping("name/{logbookName}")
	@PreAuthorize("hasAuthority('ROLE_OLOG-ADMINS')")
	@ResponseBody
	public ResponseEntity deleteLogbookByName(@PathVariable String logbookName) throws IOException {

		// TODO true delete permitted only for unused logbooks
		logger.info("delete {} logbook document from mongodb", logbookName);
		List<Logbook> logbooks = logbookRepository.findByName(logbookName);
		
		if (logbooks == null) {

			logger.debug("logbook {} not found", logbookName);
			return new ResponseEntity<>("logbook("+logbookName+") not found", HttpStatus.BAD_REQUEST);			
			
		} else if (logbooks.size() == 1) {			

			// TODO true delete permitted only for unused logbooks
			Log sparseLog = new Log();
			sparseLog.setLogbooks(logbooks);
			
			ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("logbooks.name", ExampleMatcher.GenericPropertyMatchers.exact());
			Example<Log> exampleLog = Example.of(sparseLog, matcher);
			
			long logbooksCount = logRepository.count(exampleLog);
			logger.debug("logbooksCount {}", logbooksCount);
			
			if (logbooksCount == 0) {
				logbookRepository.delete(logbooks.get(0));
				logger.info("logbook {} deleted successfully", logbookName);
			} else {
				logger.debug("logbook {} still in use", logbookName);
				return new ResponseEntity<>("logbook("+logbookName+") still in use", HttpStatus.BAD_REQUEST);
			}			
			
		} else {

			logger.warn("logbook {} found more than once.  Investigate.", logbookName);
			return new ResponseEntity<>("multiple logbooks found for name("+logbookName+")", HttpStatus.BAD_REQUEST);
			
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}
}
