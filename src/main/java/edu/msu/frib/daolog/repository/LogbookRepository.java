package edu.msu.frib.daolog.repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import edu.msu.frib.daolog.log.Logbook;

@EnableMongoRepositories(basePackages = "edu.msu.frib.daolog.repository")
public interface LogbookRepository extends MongoRepository<Logbook, String> {

	public Optional<Logbook> findById(String id);
	public List<Logbook> findByName(String name);
	public List<Logbook> findByOwner(String owner);
	public List<Logbook> findByCreatedDate(Date createdDate);
	
}
