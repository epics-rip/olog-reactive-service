package edu.msu.frib.daolog.repository;

import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import edu.msu.frib.daolog.log.Property;
import edu.msu.frib.daolog.log.State;

@EnableMongoRepositories(basePackages = "edu.msu.frib.daolog.repository")
public interface PropertyRepository extends MongoRepository<Property, String> {
	
	public Optional<Property> findById(String id);
	public Set<Property> findByName(String name);
	public Set<Property> findByOwner(String owner);
	public Set<Property> findByState(State state);
	public Set<Property> findByCreatedDate(Date date);
}
