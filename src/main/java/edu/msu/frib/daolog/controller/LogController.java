package edu.msu.frib.daolog.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.elasticsearch.common.util.set.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;

import edu.msu.frib.daolog.log.Attachment;
import edu.msu.frib.daolog.log.Log;
import edu.msu.frib.daolog.log.LogWrapper;
import edu.msu.frib.daolog.log.Logbook;
import edu.msu.frib.daolog.log.LogbooksWrapper;
import edu.msu.frib.daolog.log.Property;
import edu.msu.frib.daolog.log.State;
import edu.msu.frib.daolog.log.Tag;
import edu.msu.frib.daolog.log.TagsWrapper;
import edu.msu.frib.daolog.repository.AttachmentRepository;
import edu.msu.frib.daolog.repository.LogDBUtils;
import edu.msu.frib.daolog.repository.LogRepository;
import edu.msu.frib.daolog.repository.LogbookDBUtils;
import edu.msu.frib.daolog.repository.LogbookRepository;
import edu.msu.frib.daolog.repository.PropertyRepository;
import edu.msu.frib.daolog.repository.TagDBUtils;
import edu.msu.frib.daolog.repository.TagRepository;
import edu.msu.frib.daolog.utils.LogUtils;


@Controller
public class LogController {

	private static Logger logger = LoggerFactory.getLogger(LogController.class);

	@Autowired
	private LogRepository logRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private LogbookRepository logbookRepository;
	@Autowired
	private AttachmentRepository attachmentRepository;
	@Autowired
	private PropertyRepository propertyRepository;

	@Autowired
	MongoTemplate mongoTemplate;
	
	@Value("${olog.limit.default}")
	private String defaultLimit;
	
	@Value("${olog.log.edit.history.toggle_state}")
	private boolean configSetHistoricalLogToInactive;

	@PostMapping("/daolog/resources/log/createByParam")
	@PreAuthorize("hasAuthority('ROLE_OLOG-LOGS')")
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
	@PostMapping("/daolog/resources/logs")
	@PreAuthorize("hasAuthority('ROLE_OLOG-LOGS')")
	@ResponseBody
	public LogWrapper createLogs(@RequestBody Log[] logArray, HttpServletRequest request) throws IOException {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentPrincipalName = authentication.getName();
		
		if (logArray == null) throw new IllegalArgumentException("createLogs argument may not be null");

		LogWrapper wrapper = new LogWrapper();
		
		for (Log log : Arrays.asList(logArray)) {
			
			log.setCreatedDate(new Date());
			logger.info("log message received: " + log);
		
			log.setVersion(0);			
			log.setSource(request.getRemoteAddr());
			log.setOrigin(Generators.timeBasedGenerator(EthernetAddress.fromInterface()).generate().toString());
			log.setOwner(currentPrincipalName);
			log.setState(State.Active);
			
			if (log.getEventStart() == null) {
				log.setEventStart(log.getCreatedDate());
			}
			

			// Update the propertyIds
			List<String> propertyIds = new LinkedList<String>();
			// populate the propertyIds field from the Properties data structure
			// overriding the setProperties is done to capture RESTful creations
			log.getProperties().forEach(property -> propertyIds.add(property.getId()));
			log.setPropertyIds(propertyIds);
			log.setProperties(new ArrayList<Property>());
			
			logger.debug("log {}", log);
	
			Log savedLog = logRepository.save(log);
			logger.debug("savedLog {}", savedLog);
	
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
	public List<Log> getLogs(
			@RequestParam(value = "history", required = false) String history,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "limit", required = false) String limit,
			@RequestParam(value = "sort", required = false) String sortField,
			@RequestParam(value = "search", required = false) String search,
			@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "tag", required = false) String tag,
			@RequestParam(value = "logbook", required = false) String logbook,
			@RequestParam(value = "property", required = false) String property,
			@RequestParam(value = "owner", required = false) String owner,
			@RequestParam(value = "source", required = false) String source,
			@RequestParam(value = "start", required = false) String start,
			@RequestParam(value = "end", required = false) String end,
			@RequestParam(value = "empty", required = false) String empty
			) throws IOException {

		if (logger.isDebugEnabled()) logger.debug("find data from mongodb with the following criteria: "
				+ "page:{};limit:{};sort:{};search:{};id:{};tag:{};"
				+ "logbook:{};property:{};owner:{};source:{};start:{};end:{};empty:{}",
				page,limit,sortField,search,id,tag,logbook,property,owner,source,start,end,empty);
		
		List<Log> logs = null;
		
		try {
			Integer.valueOf(page);
			logger.debug("page: {}", page);
		} catch (NumberFormatException e) {
			page = "1";
		}

		try {
			Integer.valueOf(limit);
			logger.debug("limit: {}", limit);
		} catch (NumberFormatException e) {
			limit = defaultLimit;
		}
		
		// Pageable is zero based; logbook UI is 1 based
		Pageable pageable = new PageRequest(Integer.valueOf(page)-1, Integer.valueOf(limit));
		
		List<Criteria> orCriteria = new ArrayList<Criteria>();
		List<Criteria> andCriteria = new ArrayList<Criteria>();
		
		
		if (search  != null) orCriteria.add(Criteria.where("description").regex(search.replaceAll("\\*", "").replace(",", "|")));
		if (owner   != null) orCriteria.add(Criteria.where("owner").regex(owner));
		if (source  != null) orCriteria.add(Criteria.where("source").regex(source));
		
		if (id      != null) andCriteria.add(Criteria.where("_id").is(id));
		if (start   != null) andCriteria.add(Criteria.where("eventStart").gte(start));
		if (end     != null) andCriteria.add(Criteria.where("eventStart").lte(end));
		if (logbook != null) {
			// logbook can contain a CSV of logbook names: convert to pipes for regex
			// need to query the subdocument logbooks for names using elemMatch
			orCriteria.add(Criteria.where("logbooks").elemMatch(Criteria.where("name").in(StringUtils.commaDelimitedListToSet(logbook))));
		}
		if (tag != null) {
			// tag can contain a CSV of tag names: convert to pipes for regex
			// need to query the subdocument tags for names using elemMatch
			// TODO andCriteria.add(Criteria.where("tags").elemMatch(Criteria.where("name").regex(tag.replace(",", "|"))));
			orCriteria.add(Criteria.where("tags").elemMatch(Criteria.where("name").in(StringUtils.commaDelimitedListToSet(tag))));
		}
		
		
		Criteria queryCriteria = new Criteria();
		if (orCriteria.size() != 0) {
			queryCriteria = queryCriteria.orOperator(orCriteria.toArray(new Criteria[orCriteria.size()]));
		}
		if (andCriteria.size() != 0) {
			queryCriteria = queryCriteria.andOperator(andCriteria.toArray(new Criteria[andCriteria.size()]));
		}
		
		Sort sort = new Sort(Sort.Direction.DESC, "entry").and(new Sort(Sort.Direction.DESC, "version"));
		if (sortField != null) {
			sort = new Sort(Sort.Direction.DESC, "entry").and(new Sort(Sort.Direction.ASC, "version")).and(new Sort(Sort.Direction.DESC, (sortField.equals("created")) ? "createdDate" : sortField));
		}
				
		if (queryCriteria == null) {
			if (logger.isDebugEnabled()) logger.debug("query without criteria: {}", queryCriteria);
			logs = mongoTemplate
					.find(new Query()
						.with(pageable).with(sort),
						Log.class);
		} else {
			if (logger.isDebugEnabled()) logger.debug("query with criteria: {}", queryCriteria);
			logs = mongoTemplate
					.find(new Query()
						.with(pageable).with(sort)
						.addCriteria(queryCriteria),
						Log.class);
		}

		if (logger.isDebugEnabled()) logger.debug("log count: " + logs.size());
		for (Log log : logs) {
			if (logger.isTraceEnabled()) logger.trace("log.getId(): {}", log.getId());
			// populate tags
			log.setTags(TagDBUtils.findTags(tagRepository, log.getTagIds()));
			if (logger.isDebugEnabled()) logger.debug("tags for logid({}): {}", log.getId(), log.getTags());
			
			// populate logbooks
			LogbooksWrapper wrapper = logRepository.findLogbookByLogId(log.getId());

			Set<Logbook> logbookSet = new HashSet<Logbook>();
			logbookSet.addAll(wrapper.getLogbooks());		
			log.setLogbooks(logbookSet);

			// populate tags
			TagsWrapper tagsWrapper = logRepository.findTagsByLogId(log.getId());
			Set<Tag> tagSet = new HashSet<Tag>();
			tagSet.addAll(tagsWrapper.getTags());	
			log.setTags(tagSet);
			
			// Read the attachments based on the attachmentIds identified
			Set<Attachment> attachments = Sets.newHashSet(attachmentRepository.findAllById(log.getAttachmentIds()));
			log.setAttachments(attachments);
			
			// Read properties
			Set<Property> properties = Sets.newHashSet(propertyRepository.findAllById(log.getPropertyIds()));
			log.setProperties(properties);
			
			if (logger.isTraceEnabled()) {
				logger.trace("log: {}", log.toString());
			}
		}
	
		return logs;
	}
	
	/**
	 * PutMapping for edits.  Editing is a misnomer.  There is no editing.  There is only clarification of the parent.
	 * The child is displayed as the latest, with history permitting perusal of the older versions.
	 * @param log
	 * @param request
	 * @return
	 */
	@PutMapping("/daolog/resources/logs/{logId}")
	@PreAuthorize("hasAuthority('ROLE_OLOG-LOGS')")
	@ResponseBody
	public Log updateLog(@PathVariable("logId") String logId, @RequestBody Log newLog, HttpServletRequest request) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentPrincipalName = authentication.getName();

		if (newLog == null) throw new IllegalArgumentException("createLogs argument may not be null");	
		if (!newLog.getId().equals(logId)) throw new IllegalArgumentException("log id does not match log id of object json");	

		logger.info("newLog: " + newLog.toString());
		
		// Retrieve the original log from the database
		Log originalLog = LogDBUtils.findLog(logRepository, logId);

		newLog.setId(null); // necessary to generate new log entry
		newLog = LogUtils.updateLogEntry(newLog, originalLog);
		newLog = LogUtils.updateLogVersion(newLog, originalLog);

		newLog.setSource(request.getRemoteAddr());
		newLog.setOrigin(Generators.timeBasedGenerator(EthernetAddress.fromInterface()).generate().toString());
		newLog.setOwner(currentPrincipalName);
		newLog.setState(State.Active);
		newLog.setCreatedDate(new Date());
		
		// Save the object to the database		
		Log savedNewLog = logRepository.save(newLog);
		
		// Save the original log entry's state to Inactive
		if (configSetHistoricalLogToInactive) {
			originalLog.setState(State.Inactive);
			logRepository.save(originalLog);
		}
		
		return savedNewLog;
	}

	@GetMapping("/daolog/resources/logs/{logId}")
	@ResponseBody
	public Log getLog(@PathVariable("logId") String logId) throws IOException {

		logger.info("findAll() from mongodb!");
		Optional<Log> log = logRepository.findById(logId);

		// populate tags
		log.get().setTags(TagDBUtils.findTags(tagRepository, log.get().getTagIds()));

		// populate logbook
		log.get().setLogbooks(LogbookDBUtils.findLogbooks(logbookRepository, log.get().getLogbookIds()));

		return log.get();
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseBody
	public String handleMissingParamsException(MissingServletRequestParameterException ex) {
		logger.info("User called interface API without necessary parameters: " + ex.getParameterName());
		return "There was a problem serving your request.  You are missing required parameters: "
				+ ex.getParameterName();
	}
}
