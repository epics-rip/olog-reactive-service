package edu.msu.frib.daolog.repository;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.msu.frib.daolog.log.Log;

public class LogMapperTest {

	public static void main (String[] args) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		ArrayNode logs = mapper.createArrayNode();
		ArrayNode logbooks = mapper.createArrayNode();
		ArrayNode properties = mapper.createArrayNode();
		ArrayNode tags = mapper.createArrayNode();
		ArrayNode attachments = mapper.createArrayNode();
		
		String json = "[{\"description\":\"another entry\",\"logbooks\":[{\"name\":\"LOGBOOK1\"}],\"tags\":[],\"properties\":[],\"attachments\":[],\"level\":\"Info\",\"eventStart\":\"\"}]";
		
		ObjectNode log = mapper.createObjectNode();
		log.put("description", "another entry");
		log.put("level", "Info");
		log.put("eventStart", "");
		

		ObjectNode logbook1 = mapper.createObjectNode();
		logbook1.put("name", "LOGBOOK1");
		logbooks.add(logbook1);
		log.put("logbooks", logbooks);
		log.put("properties", properties);
		log.put("tags", tags);
		log.put("attachments", attachments);
		
		logs.add(log);
		
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(logs));
		System.out.println("");
		
		try {
			mapper.readValue(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(logs), Log[].class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
