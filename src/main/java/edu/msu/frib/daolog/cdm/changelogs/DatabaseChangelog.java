package edu.msu.frib.daolog.cdm.changelogs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.mongodb.client.MongoDatabase;

import edu.msu.frib.daolog.log.Log;
import edu.msu.frib.daolog.log.Logbook;
import edu.msu.frib.daolog.log.Tag;
import edu.msu.frib.daolog.repository.LogDBUtils;
import edu.msu.frib.daolog.repository.LogbookDBUtils;
import edu.msu.frib.daolog.repository.TagDBUtils;

/**
 * Proof of concept for mongobee continuous database migration
 * This only creates and updates records, but it could be used to add indexes, rename collections, etc.
 * 
 * This does nothing useful at this time, but it is functional, and easily extended going forward.
 * 
 * Change this text when this situation changes.
 * 
 * To turn this operation on, you must add the following to the VM Arguments:  -Dspring.profiles.active=development
 * Setting this in application.properties does not work; the reason for this is unknown to me at this time
 * 
 * @author vagrant
 *
 */
@ChangeLog(order = "001")
@Profile("development")
public class DatabaseChangelog {

	private static Logger logger = LoggerFactory.getLogger(DatabaseChangelog.class);
			
	private Set<Tag> populatedTags;
	private Set<Logbook> populatedLogbooks;
	private Set<Log> populatedLogs;

	@ChangeSet(order = "001", id = "dropDatabase", author = "carrivea", runAlways=true)
	public void deleteExistingDB(MongoDatabase db) {
		
		logger.debug("starting database delete process...");
		
		// delete the database and start fresh
		db.drop();
	}

	@ChangeSet(order = "002", id = "loadBasicTags", author = "carrivea")
	public void loadBasicTags(MongoTemplate mongoTemplate) {

		logger.debug("starting loadBasicTags process...");
		
		// read json files
		Resource tagFile = new ClassPathResource("db/tags.json");
		InputStream tagsStream;
		
		try {
			tagsStream = tagFile.getInputStream();
			this.populatedTags = TagDBUtils.insertTags(mongoTemplate, tagsStream);

		} catch (IOException e) {
			logger.error("failed to load data for tags!");
			e.printStackTrace();
		}
	}

	@ChangeSet(order = "003", id = "loadBasicLogbooks", author = "carrivea")
	public void loadBasicLogbooks(MongoTemplate mongoTemplate) {

		logger.debug("starting loadBasicLogbooks process...");
		
		// read json files
		Resource logbookFile = new ClassPathResource("db/logbooks.json");
		InputStream logbooksStream;
		
		try {
			logbooksStream = logbookFile.getInputStream();
			this.populatedLogbooks = LogbookDBUtils.insertLogbooks(mongoTemplate, logbooksStream);
		} catch (IOException e) {
			logger.error("failed to load data for logbooks!");
			e.printStackTrace();
		}
	}

	@ChangeSet(order = "004", id = "loadBasicLogs", author = "carrivea")
	public void loadBasicLogs(MongoTemplate mongoTemplate) {

		logger.debug("starting loadBasicLogs process...");
		
		// read json files
		Resource logFile = new ClassPathResource("db/logs.json");
		InputStream logsStream;
		
		try {
			logsStream = logFile.getInputStream();
			this.populatedLogs = LogDBUtils.insertLogs(mongoTemplate, logsStream);
			
			for (Log log : populatedLogs) {
				log.setEntry(log.getId());
				log.setVersion(0);
				log.setCreatedDate(new Date());
				mongoTemplate.save(log);
			}
		} catch (IOException e) {
			logger.error("failed to load data for logs!");
			e.printStackTrace();
		}
	}
	
	@ChangeSet(order = "005", id = "loadExtendedLogs", author = "carrivea")
	public void loadExtendedLogs(MongoTemplate mongoTemplate) throws IOException {

		logger.debug("starting loadExtendedLogs process...this.populatedLogs.size()=="+this.populatedLogs.size());

		Resource logFile = new ClassPathResource("db/logs-extended.json");
		
		InputStream logsStream;

		try {
			logsStream = logFile.getInputStream();			

			// Now sprinkle in tags, logbooks, and other things to complete the objects 
			String logsExtended = IOUtils.toString(logsStream, StandardCharsets.UTF_8.name());
			
			int i = 1;
			for (Logbook lb : populatedLogbooks) {
				logsExtended = logsExtended.replaceAll("##logbookids" + i++ + "##", lb.getId());
			}
			i = 1;
			for (Tag tag : populatedTags) {
				logsExtended = logsExtended.replaceAll("##tagids" + i++ + "##", tag.getId());
			}
			
			logger.debug(logsExtended);
			
			InputStream modifiedLogsStream = new ByteArrayInputStream(logsExtended.getBytes());
			
			this.populatedLogs = LogDBUtils.insertLogs(mongoTemplate, modifiedLogsStream);

			for (Log log : populatedLogs) {
				log.setEntry(log.getId());
				log.setVersion(0);
				log.setCreatedDate(new Date());
				mongoTemplate.save(log);
			}

			// checks all logs for completeness again
			for (Log log : populatedLogs) {
				if (log.getEntry() == null || log.getEntry().isEmpty()) {
					log.setEntry(log.getId());
				}
				if (log.getCreatedDate() == null) {
					log.setCreatedDate(new Date());
				}
				mongoTemplate.save(log);
			}
			
		} catch (IOException e) {
			logger.error("failed to load data for extended logs!");
			e.printStackTrace();
		}
		
	}

	@ChangeSet(order = "006", id = "loadRevisedLogs", author = "carrivea")
	public void loadRevisedLogs(MongoTemplate mongoTemplate) throws IOException {

		logger.debug("starting loadRevisedLogs process...this.populatedLogs.size()=="+this.populatedLogs.size());

		Resource logFile = new ClassPathResource("db/logs-revised.json");
		
		InputStream logsStream;
		List<String> baseLogIds = new LinkedList<String>();

		try {
			logsStream = logFile.getInputStream();			

			// Now sprinkle in tags, logbooks, and other things to complete the objects 
			String logsExtended = IOUtils.toString(logsStream, StandardCharsets.UTF_8.name());

			int i = 1;
			for (Logbook lb : populatedLogbooks) {
				logsExtended = logsExtended.replaceAll("##logbookids" + i++ + "##", lb.getId());
			}
			i = 1;
			for (Tag tag : populatedTags) {
				logsExtended = logsExtended.replaceAll("##tagids" + i++ + "##", tag.getId());
			}
			i = 1;
			for (Log log : populatedLogs) {
				baseLogIds.add(log.getId());
				logsExtended = logsExtended.replaceAll("##entry" + i + "##", log.getId());
				logsExtended = logsExtended.replaceAll("##entry" + i + "description##", log.getDescription());
				i++;
			}
			
			logger.debug(logsExtended);
			
			InputStream modifiedLogsStream = new ByteArrayInputStream(logsExtended.getBytes());
			
			this.populatedLogs = LogDBUtils.insertLogs(mongoTemplate, modifiedLogsStream);
			
			// set the version number for the revised/child logs
			Query findBaseIds = new Query();
			findBaseIds.addCriteria(Criteria.where("description").regex(" revised!"));
			List<Log> logs = mongoTemplate.find(findBaseIds, Log.class);
			for (Log log : logs) {
				
				// Query for all the logs that have entry = logId
				Log revisedLog = mongoTemplate
						.findAndModify(new Query()
								.addCriteria(Criteria
										.where("id")
										.is(log.getId())
										.and("version")
										.is(null)),
								new Update().set("version", 1).set("createdDate", new Date()),
								Log.class);
				
			}

			
		} catch (IOException e) {
			logger.error("failed to load data for extended logs!");
			e.printStackTrace();
		}
		
	}

}
