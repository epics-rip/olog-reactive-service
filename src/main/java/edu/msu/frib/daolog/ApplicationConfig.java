package edu.msu.frib.daolog;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@EnableAutoConfiguration // create MongoTemplate and MongoOperations
@EnableMongoRepositories(basePackages = "edu.msu.frib.daolog.repository")
public class ApplicationConfig {

	
    @Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)  
          .select()                                  
          .apis(RequestHandlerSelectors.basePackage("edu.msu.frib.daolog.controller"))           
          .paths(PathSelectors.ant("/daolog/*"))                        
          .build();                                           
    }
}
