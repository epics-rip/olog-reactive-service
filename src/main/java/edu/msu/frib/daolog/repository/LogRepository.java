package edu.msu.frib.daolog.repository;

import edu.msu.frib.daolog.log.Log;

import java.sql.Date;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


public interface LogRepository extends MongoRepository<Log, String>{

	public Log findById(String id);
	public List<Log> findByOwner(String owner);
	public List<Log> findByCreatedDate(Date createdDate);
	public List<Log> findByDescription(String description);
	public List<Log> findByLogbookIds(List<String> logbookIds);
	public Log findFirstByEntryOrderByVersionDesc(String entryId);

}
