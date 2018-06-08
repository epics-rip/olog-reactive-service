package edu.msu.frib.daolog.db;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "edu.msu.frib.daolog.repository")
@Profile("development")
public class MongoInMemoryConfig {
	// Empty class, using mongodb auto-configuration from properties file
}
