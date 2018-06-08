package edu.msu.frib.daolog.repository;

import java.sql.Date;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import edu.msu.frib.daolog.log.State;
import edu.msu.frib.daolog.log.Tag;

@EnableMongoRepositories(basePackages = "edu.msu.frib.daolog.repository")
public interface TagRepository extends MongoRepository<Tag, String> {

	public Optional<Tag> findById(String id);
	public Set<Tag> findByName(String name);
	public Set<Tag> findByOwner(String owner);
	public Set<Tag> findByState(State state);
	public Set<Tag> findByCreatedDate(Date createdDate);
}
