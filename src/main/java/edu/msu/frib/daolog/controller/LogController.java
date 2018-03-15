package edu.msu.frib.daolog.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.msu.frib.daolog.log.Log;
import edu.msu.frib.daolog.log.LogWrapper;
import edu.msu.frib.daolog.log.State;
import edu.msu.frib.daolog.repository.LogDBUtils;
import edu.msu.frib.daolog.repository.LogRepository;
import edu.msu.frib.daolog.repository.LogbookDBUtils;
import edu.msu.frib.daolog.repository.LogbookRepository;
import edu.msu.frib.daolog.repository.TagDBUtils;
import edu.msu.frib.daolog.repository.TagRepository;


@Controller
public class LogController {

	private static Logger logger = LoggerFactory.getLogger(LogController.class);

	@Autowired
	private LogRepository logRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private LogbookRepository logbookRepository;

	@PostMapping("/daolog/resources/log/createByParam")
	@ResponseBody
	public Log testCreateLog(@RequestParam(value = "description", required = true) String description,
			@RequestParam(value = "owner", required = true) String owner,
			@RequestParam(value = "logbook", required = true) String logbook,
			@RequestParam(value = "tag", required = false) String tag,
			@RequestParam(value = "property", required = false) String property,
			@RequestParam(value = "source", required = true) String source,
			@RequestParam(value = "state", required = true) String stateString,
			@RequestParam(value = "level", required = false) String level,
			@RequestParam(value = "version", required = false) String version,
			@RequestParam(value = "start", required = false) String start,
			@RequestParam(value = "end", required = false) String end) throws IOException {

		Date now = new Date();
		java.sql.Date createdDate = new java.sql.Date(now.getTime());

		State state = State.valueOf(stateString);

		// Logbook is multi-valued.
		List<String> logbooks = new LinkedList<String>();
		logbooks.addAll(Arrays.asList(logbook.split(";")));

		// TODO create the entity beans that will hold these values for subsequent use
		// later
		// Use RxJava2 to manage the transfer of the data to mongodb, ES, and Kafka.
		// TODO also define a value indicating whether the data has been fully processed
		// in ES and Kafka and database

		Log msg = new Log.Builder(description, owner, source, logbooks, state, now).build();

		// save the message
		Log savedLog = logRepository.save(msg);

		logger.info("message saved to mongodb: " + savedLog);

		return savedLog;
	}

	/**
	 * Log creation from a sparse Log object submitted by REST in JSON format
	 * 
	 * If a log is new, it's entry will be updated to the id field, and the version
	 * will be set to 0. If a log creation has an entry already populated, then this
	 * is an addendum, and the version number will be incremented.
	 * 
	 * Log creations will not have Tags or Logbooks populated. These get populated
	 * on getLogs() requests
	 * 
	 * @param log
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/daolog/resources/log/createByJSON")
	@ResponseBody
	public Log createLogJSON(@RequestBody Log log) throws IOException {

		log.setCreatedDate(new Date());
		logger.info("log message received: " + log);

		// TODO validate the logbook BSON IDs provided?
		// TODO validate the tag BSON IDs provided?
		// TODO validate the entry BSON id provided?l

		// Determine version number for the log message
		// must query for the existing entry if populated, get the maximum version, then
		// increment it.
		if (log.getEntry() != null && !log.getEntry().isEmpty()) {
			logger.debug("a sublog has been entered to log entry: " + log.getEntry());
			Integer newVersion = ((LogDBUtils.findLogWithMaxVersion(logRepository, log)).getVersion()) + 1;
			log.setVersion(newVersion); // increment the version number in the condition of a pre-existing value existed
		} else {
			log.setVersion(0);
		}

		Log savedLog = logRepository.save(log);

		// Upon a log message creation, update the log to include it's own BSON id as
		// the collection entry value
		if (savedLog.getEntry() == null || savedLog.getEntry().isEmpty()) {
			savedLog.setEntry(savedLog.getId());
			logRepository.save(savedLog);
		}

		// Use RxJava2 to manage the transfer of the data to mongodb, ES, and Kafka.
		// TODO also define a value indicating whether the data has been fully processed
		// in ES and Kafka and database

		logger.info("log message saved to mongodb: " + savedLog);
		return savedLog;
	}
	
	@PostMapping("/daolog/resources/logs/createByJSON")
	@ResponseBody
	public Set<Log> createLogs(InputStream logsStream) throws JsonParseException, JsonMappingException, IOException {
		
		// call MongoDB to insert them all into the database
		Set<Log> savedLogs = LogDBUtils.insertLogs(logRepository, logsStream);		
		return savedLogs; 
	}
	

	
	@PostMapping("/daolog/resources/login")
	public ResponseEntity login() throws JsonParseException, JsonMappingException, IOException {
		
		// call MongoDB to insert them all into the database
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentPrincipalName = authentication.getName();
		
		logger.info("user " + currentPrincipalName);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}

	
	@PostMapping("/daolog/resources/logs")
	@ResponseBody
	public LogWrapper createLogs(@RequestBody Log[] logArray) throws IOException {

		if (logArray == null) throw new IllegalArgumentException("createLogs argument may not be null");

		LogWrapper wrapper = new LogWrapper();
		
		for (Log log : Arrays.asList(logArray)) {
			
			log.setCreatedDate(new Date());
			logger.info("log message received: " + log);
		
			// Determine version number for the log message
			// must query for the existing entry if populated, get the maximum version, then
			// increment it.
			if (log.getEntry() != null && !log.getEntry().isEmpty()) {
				logger.debug("a sublog has been entered to log entry: " + log.getEntry());
				Integer newVersion = ((LogDBUtils.findLogWithMaxVersion(logRepository, log)).getVersion()) + 1;
				log.setVersion(newVersion); // increment the version number in the condition of a pre-existing value existed
			} else {
				log.setVersion(0);
			}
	
			Log savedLog = logRepository.save(log);
	
			// Upon a log message creation, update the log to include it's own BSON id as
			// the collection entry value
			if (savedLog.getEntry() == null || savedLog.getEntry().isEmpty()) {
				savedLog.setEntry(savedLog.getId());
				logRepository.save(savedLog);
				wrapper.getLog().add(savedLog);
			}
	
			// Use RxJava2 to manage the transfer of the data to mongodb, ES, and Kafka.
			// TODO also define a value indicating whether the data has been fully processed
			// in ES and Kafka and database
	
			logger.info("log message saved to mongodb: " + savedLog);
		}
		return wrapper;
	}

	@GetMapping("/daolog/resources/logs")
	@ResponseBody
	public List<Log> getLogs(@RequestParam(value = "history", required = false) String history,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "limit", required = false) String limit,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "tag", required = false) String tag,
			@RequestParam(value = "logbook", required = false) String logbook,
			@RequestParam(value = "property", required = false) String property,
			@RequestParam(value = "owner", required = false) String owner,
			@RequestParam(value = "source", required = false) String source,
			@RequestParam(value = "start", required = false) String start,
			@RequestParam(value = "end", required = false) String end,
			@RequestParam(value = "empty", required = false) String empty) throws IOException {

		// validate input
		// check input for valid input and valid ranges, and throw LogsException if
		// necessary

		// Query database for log messages
		// TODO For now, merely query mongodb for all records, however, will will be
		// changing this to streaming
		// How to implement streaming for this? One page worth at a time...
		// TODO will stream the data using reactive streams...
		logger.info("findAll() from mongodb!");
		List<Log> logs = logRepository.findAll();

		// XXX sort=options probably defined in database
		// TODO limit=number of records to display in one page
		// TODO page=index of the set of records returned being displayed right now
		// TODO history=unknown; probably a date to query by...this might be where the
		// one requirement about going back in time like a pointer comes in handy;
		// currently it might just query for that date...investigate

		// TODO populate the logbooks and tags via manual referencing suggested by
		// mongodb
		// this is the classic n+1 problem, and in MongoDB, it's particularly
		// problematic
		// for large n, this will be a flood of queries
		for (Log log : logs) {

			// populate tags
			log.setTags(TagDBUtils.findTags(tagRepository, log.getTagIds()));
			// populate logbooks
			log.setLogbooks(LogbookDBUtils.findLogbooks(logbookRepository, log.getLogbookIds()));
		}

		StringBuffer buff = new StringBuffer();
		logger.info("log count: " + logs.size());
		buff.append("log count: " + logs.size() + "<br/><p/>");

		for (Log log : logs) {
			buff.append("owner: " + log.getOwner() + "<br/>");
			buff.append("description: " + log.getDescription() + "<br/>");
			buff.append("<p/>");
		}

		buff.append(
				"<br/>return page=" + page + "; history=" + history + "; limit=" + limit + "; sort=" + sort + "<br/>");

		logger.info(buff.toString());
		return logs;
	}

	@GetMapping("/daolog/resources/logs/{logId}")
	@ResponseBody
	public Log getLog(@PathVariable("logId") String logId) throws IOException {

		logger.info("findAll() from mongodb!");
		Log log = logRepository.findById(logId);

		// populate tags
		log.setTags(TagDBUtils.findTags(tagRepository, log.getTagIds()));

		// populate logbook
		log.setLogbooks(LogbookDBUtils.findLogbooks(logbookRepository, log.getLogbookIds()));

		return log;
	}

	@GetMapping("/daolog/delete-logs")
	@ResponseBody
	public void deleteAllLogs() throws IOException {

		logger.info("delete all log documents from mongodb");
		logRepository.deleteAll();

	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseBody
	public String handleMissingParamsException(MissingServletRequestParameterException ex) {
		logger.info("User called interface API without necessary parameters: " + ex.getParameterName());
		return "There was a problem serving your request.  You are missing required parameters: "
				+ ex.getParameterName();
	}
}
