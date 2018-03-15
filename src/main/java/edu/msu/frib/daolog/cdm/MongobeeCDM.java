package edu.msu.frib.daolog.cdm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.github.mongobee.Mongobee;

@Configuration
@PropertySource("classpath:mongodb.properties")
public class MongobeeCDM {

	private static Logger logger = LoggerFactory.getLogger(MongobeeCDM.class);

	@Bean @Autowired
	public Mongobee mongobee(Environment config) {

		logger.debug("starting mongobee continuous database migrating tool...URI: " + "mongodb://" + config.getProperty("mongo.host") + ":"
				+ config.getProperty("mongo.port") + "/" + config.getProperty("mongo.database"));
		
		Mongobee runner = new Mongobee("mongodb://" + config.getProperty("mongo.host") + ":"
				+ config.getProperty("mongo.port"));

		runner.setDbName(config.getProperty("mongo.database"));
		runner.setSpringEnvironment(config);
		logger.debug("spring environment: " + config.toString());
		runner.setChangeLogsScanPackage("edu.msu.frib.daolog.cdm.changelogs"); // the package to be scanned for
																				// changesets
		
		logger.debug("ending mongobee method, returning the runner...isEnabled()? " + runner.isEnabled());
		return runner;
	}
}
