package edu.msu.frib.daolog.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.msu.frib.daolog.exception.EmptySetException;
import edu.msu.frib.daolog.exception.TooManyElementsException;
import edu.msu.frib.daolog.log.Log;
import edu.msu.frib.daolog.log.Property;
import edu.msu.frib.daolog.repository.LogRepository;
import edu.msu.frib.daolog.repository.PropertyRepository;
import edu.msu.frib.daolog.utils.DataUtils;

@Controller
@RequestMapping("/daolog/resources/properties/")
public class PropertyController {
	private static Logger logger = LoggerFactory.getLogger(PropertyController.class);
	

	@Autowired
	private PropertyRepository propertyRepository;
	@Autowired
	private LogRepository logRepository;
	
	/**
	 * GET method to retrieve property
	 * If no records found, return 200 with empty body
	 * If too  many records found, return 200 with empty body
	 * 
	 */
	@GetMapping("{propertyName}")
	@ResponseBody
	public Property getProperty(@PathVariable String propertyName) {
		
		logger.info("getProperty...propertyName == {}", propertyName);
		Set<Property> propertySet = propertyRepository.findByName(propertyName);
		
		Property property = null;
		try {
			property = DataUtils.getOnlyElement(propertySet);
			logger.info("property id: {}", property.getId());
		} catch (EmptySetException e) {
			logger.debug("No properties found for {}: {}", propertyName, e.getMessage() );
		} catch (TooManyElementsException e) {
			logger.debug("Too many properties found for {}: {}", propertyName, e.getMessage() );
		}
				
		return property;
	}
	
	
	/**
	 * PUT method to add a new OLOG property.  An exception will be throw if a property exists with the same name.
	 * TODO make name unique, and hide their true names through the interface 
	 * @param propertyName
	 * @param properties
	 * @return
	 */
	@PutMapping("{propertyName}")
	@PreAuthorize("hasAuthority('ROLE_OLOG-LOGS')")
	@ResponseBody
	public String createProperty(@PathVariable String propertyName, @RequestBody Property property) {
		
		logger.info("createProperty...properties.getName() == {}", property.getName());
		
		Property savedProperty = propertyRepository.save(property);
		
		return savedProperty.getId();
	}

	/**
	 * POST method to update an OLOG property.  Replace sparse property fields that exist.
	 * Return update property
	 * @param propertyName
	 * @param properties
	 * @return
	 */
	@PostMapping("{propertyName}")
	@PreAuthorize("hasAuthority('ROLE_OLOG-ADMINS')")
	public Property updateProperty(@PathVariable String propertyName, @RequestBody Property property) {
		
		logger.info("updateProperty...propertyName == {}", propertyName);

		ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("name", ExampleMatcher.GenericPropertyMatchers.exact());
		Example<Property> exampleproperty = Example.of(new Property(propertyName), matcher);
		
		// TODO find specified property
		Optional<Property> optionalProperty = propertyRepository.findOne(exampleproperty);
		
		// TODO transfer any changes from the sparse property to the existing property
		Property originalProperty = null;
		if (optionalProperty.isPresent()) {
			originalProperty = optionalProperty.get();
		}
		if (property.getName() != null && !property.getName().isEmpty()) {
			originalProperty.setName(property.getName());
		}
		if (property.getOwner() != null && !property.getOwner().isEmpty()) {
			originalProperty.setOwner(property.getOwner());
		}
		if (property.getCreatedDate() != null) {
			originalProperty.setCreatedDate(property.getCreatedDate());
		}
		if (property.getState() != null) {
			originalProperty.setState(property.getState());
		}
		// TODO save property		
		return propertyRepository.save(originalProperty);
		
	}

	/**
	 * DELETE method to delete an OLOG property.
	 * @param propertyName
	 * @param properties
	 * @return 
	 * @return
	 */
	@DeleteMapping("{propertyName}")
	@PreAuthorize("hasAuthority('ROLE_OLOG-ADMINS')")
	@ResponseBody
	public ResponseEntity<Object> deleteProperty(@PathVariable String propertyName) {
		
		logger.info("deleteProperty...propertyName == {}", propertyName);
		
		// Properties OUGHT to be 1:1 with logs
		// Property TEMPLATES will be 1:n with logs
		
		ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("name", ExampleMatcher.GenericPropertyMatchers.exact());
		Example<Property> exampleproperty = Example.of(new Property(propertyName), matcher);
		
		Set<Property> propertySet = propertyRepository.findByName(propertyName);

		Property property = null;
		try {
			property = DataUtils.getOnlyElement(propertySet);
			logger.info("property id: {}", property.getId());
			// delete the property
			propertyRepository.delete(property);
			
			// remove the propertyIds from logs
			List<Log> logs = logRepository.findLogByPropertyId(property.getId());
			
			// TODO remove the id from all the propertiesId fields everywhere
			for(Log log: logs) {
				log.getPropertyIds().remove(property.getId());
				logRepository.save(log);
			}
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (EmptySetException e) {
			logger.debug("No properties found for {}: {}", propertyName, e.getMessage() );
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		} catch (TooManyElementsException e) {
			logger.debug("Too many properties found for {}: {}", propertyName, e.getMessage() );
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
		}
		
	}
}
