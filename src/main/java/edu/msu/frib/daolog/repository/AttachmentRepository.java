package edu.msu.frib.daolog.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import edu.msu.frib.daolog.log.Attachment;
import edu.msu.frib.daolog.log.Log;

@EnableMongoRepositories(basePackages = "edu.msu.frib.daolog.repository")
public interface AttachmentRepository extends MongoRepository<Attachment, String>{

	public Log findById(String id);
}
