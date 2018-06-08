package edu.msu.frib.daolog.repository;

import edu.msu.frib.daolog.log.Log;
import edu.msu.frib.daolog.log.LogbooksWrapper;
import edu.msu.frib.daolog.log.TagsWrapper;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;


public interface LogRepository extends MongoRepository<Log, String>{

	public Optional<Log> findById(String id);
	public List<Log> findByOwner(String owner);
	public List<Log> findByCreatedDate(Date createdDate);
	public List<Log> findByDescription(String description);
	public List<Log> findByLogbookIds(List<String> logbookIds);
	public Log findFirstByEntryOrderByVersionDesc(String entryId);
	
	@Query(value="{_id: ?0}", fields="{logbooks:1, _id:0}")
	public LogbooksWrapper findLogbookByLogId(String id);

	@Query(value="{_id: ?0}", fields="{tags:1, _id:0}")
	public TagsWrapper findTagsByLogId(String id);
	
//	@Query(value="{_id: ?0}", fields="{logbooks:1, _id:0}")
//	public Set<Logbook> findLogByLogId(String id);
	
	@Query(value="{\"logs.propertiesIds\": {$regex: ?0} }")
	public List<Log> findLogByPropertyId(String propertyId);
}
