package edu.msu.frib.daolog.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Configuration
@EnableMongoRepositories(basePackages = "edu.msu.frib.daolog.repository")
@PropertySource("classpath:mongodb.properties")
public class MongoDBDaologConfig extends AbstractMongoConfiguration {

	@Autowired
	private Environment config;
	  
	@Override
	protected String getDatabaseName() {
		return config.getProperty("mongo.database");
	}

	@Override
	public Mongo mongo() throws Exception {
		return new MongoClient(config.getProperty("mongo.host"),
				Integer.parseInt(config.getProperty("mongo.port")));
	}
	
	@Override
    protected String getMappingBasePackage() {
        return "edu.msu.frib.daolog.log";
    }
	
	/**
	 * Required for JUnit to connect to the mongodb properly...
	 */
	@Bean
	public MongoTemplate mongoTemplate() throws Exception {
		return new MongoTemplate(new MongoClient(config.getProperty("mongo.host"),
				Integer.parseInt(config.getProperty("mongo.port"))), "daolog");
	}
}
