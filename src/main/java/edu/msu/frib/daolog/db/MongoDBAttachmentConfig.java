package edu.msu.frib.daolog.db;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import edu.msu.frib.daolog.repository.AttachmentRepository;

@Configuration
@EnableMongoRepositories(basePackageClasses=AttachmentRepository.class, basePackages = "edu.msu.frib.daolog.repository")
public class MongoDBAttachmentConfig
{
	// Empty class, using mongodb auto-configuration from properties file
}
