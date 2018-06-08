package edu.msu.frib.daolog.repository;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;



@Profile("development")
@Configuration
public class LogbookControllerTestConfiguration {
	
	
	@Bean
    @Primary
    public LogbookRepository logbookRepository() {
		System.out.println("inside logbookRepository() mocking the logbookRepository object...");
        return Mockito.mock(LogbookRepository.class);
    }
}
