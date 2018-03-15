package edu.msu.frib.daolog.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;

@Configuration
@EnableMongoRepositories(basePackages = "edu.msu.frib.daolog.repository")
@PropertySource("classpath:mongodb.properties")
public class MongoDBAttachmentConfig extends AbstractMongoConfiguration {

	@Autowired
	private Environment config;
	
	@Bean
	public GridFsTemplate gridFsTemplate() throws Exception {
		return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
	}
	
	@Override
	protected String getDatabaseName() {
		// TODO Auto-generated method stub
		return config.getProperty("mongo.database");
	}

	@Override
	public Mongo mongo() throws Exception {
		// TODO Auto-generated method stub
		return new MongoClient(config.getProperty("mongo.host"),
				Integer.parseInt(config.getProperty("mongo.port")));
	}

}
