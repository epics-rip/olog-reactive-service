package edu.msu.frib.daolog.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import edu.msu.frib.daolog.log.Attachment;

@EnableMongoRepositories(basePackages = "edu.msu.frib.daolog.repository")
public interface AttachmentRepository extends MongoRepository<Attachment, String>{

	public Optional<Attachment> findById(String id);
		
	@Query("{ 'filename' : { $regex: ?0 } }")
	public Set<Attachment> findAttachmentByRegexpLogId(String regexp);
	
	@Query("{ 'filename' : ?0 }")
	public Set<Attachment> findAttachmentByFilename(String filename);
}
