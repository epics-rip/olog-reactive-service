package edu.msu.frib.daolog.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.thymeleaf.util.StringUtils;

import edu.msu.frib.daolog.Application;
import edu.msu.frib.daolog.controller.LogbookController;
import edu.msu.frib.daolog.log.Logbook;
import edu.msu.frib.daolog.log.LogbookWrapper;
import edu.msu.frib.daolog.log.State;

//ActiveProfiles("test")
//RunWith(SpringJUnit4ClassRunner.class)
//SpringBootTest(classes = Application.class)
public class LogbookControllerIntegrationTest {

	@Autowired
	private LogbookController logbookController;
	
	@Autowired
	private LogbookRepository logbookRepository;
	
	public final String logbooknameBase = "LOGBOOKTEST";
	
	//Test
    public void whenLogbookWrapperIsProvided_thenCreateLogbooks() {
		
		List<Logbook> logbooks = new ArrayList<Logbook>();
		
		// Mock up the database 

		String logbookname = logbooknameBase+StringUtils.randomAlphanumeric(4);
		Logbook mockedLogbook = new Logbook(logbooknameBase, "carrivea", State.Active);
		mockedLogbook.setId("5ace24041970943fd780a751");
		logbooks.add(mockedLogbook);

		// Mocking the database call
		Mockito.when(logbookRepository.save(new Logbook(logbookname, "carrivea", State.Active))).thenReturn(mockedLogbook);
		logbookRepository.delete(logbookRepository.findByName(logbookname).get(0));

		// Load the mocked logbook into the wrapper to go to the controller
		LogbookWrapper incomingLogbookWrapper = new LogbookWrapper();
		incomingLogbookWrapper.setLogbook(logbooks);
        
		// call the createLogbook method on the controller
        LogbookWrapper savedLogbookWrapper = logbookController.createLogbook(incomingLogbookWrapper);
        
        Assert.assertEquals(savedLogbookWrapper.getLogbook().size(), incomingLogbookWrapper.getLogbook().size());
    }
	
	//Test
    public void whenLogbookRepositoryIsProvided_thenCreateLogbooksCheckDate() {
		
		List<Logbook> logbooks = new ArrayList<Logbook>();
		
		// Mock up the database 
		String logbookname = logbooknameBase+StringUtils.randomAlphanumeric(4);
		Logbook mockedLogbook = new Logbook(logbooknameBase, "carrivea", State.Active);
		mockedLogbook.setId("5ace24041970943fd780a751");
		logbooks.add(mockedLogbook);

		// Mocking the database call
		Mockito.when(logbookRepository.save(new Logbook(logbookname, "carrivea", State.Active))).thenReturn(mockedLogbook);
		logbookRepository.delete(logbookRepository.findByName(logbookname).get(0));

		// Load the mocked logbook into the wrapper to go to the controller
		LogbookWrapper incomingLogbookWrapper = new LogbookWrapper();
		incomingLogbookWrapper.setLogbook(logbooks);
        
		// call the createLogbook method on the controller
        LogbookWrapper savedLogbookWrapper = logbookController.createLogbook(incomingLogbookWrapper);
        
        Assert.assertTrue(savedLogbookWrapper.getLogbook().get(0).getCreatedDate() != null);
    }
	
	//Test
    public void whenLogbookRepositoryIsProvided_thenGetLogbooks() {
		
		List<Logbook> logbooks = new ArrayList<Logbook>();
		
		// Mock up the database 
		String logbookname = logbooknameBase+StringUtils.randomAlphanumeric(4);
		Logbook mockedLogbook = new Logbook(logbooknameBase, "carrivea", State.Active);
		mockedLogbook.setId("5ace24041970943fd780a751");
		logbooks.add(mockedLogbook);

		// Mocking the database call
		Mockito.when(logbookRepository.findAll()).thenReturn(logbooks);
        
		// call the createLogbook method on the controller
        LogbookWrapper savedLogbookWrapper = logbookController.getLogbooks();
        
        // Confirms the method works as expected
        Assert.assertEquals(savedLogbookWrapper.getLogbook(), logbooks);
    }
	
	//Test
    public void whenLogbookRepositoryIsProvided_thenDeleteLogbookByName() {
		List<Logbook> logbooks = new ArrayList<Logbook>();
		
		// Mock up the database 
		String logbookname = logbooknameBase+StringUtils.randomAlphanumeric(4);
		Logbook mockedLogbook = new Logbook(logbooknameBase, "carrivea", State.Active);
		mockedLogbook.setId("5ace24041970943fd780a751");
		logbooks.add(mockedLogbook);

		// Mocking the database call
		Mockito.doNothing().when(logbookRepository).deleteById("5ace24041970943fd780a751");
        
		// call the createLogbook method on the controller
        try {
			logbookController.deleteLogbookById("5ace24041970943fd780a751");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
}
