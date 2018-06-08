package edu.msu.frib.daolog.log;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TagWrapper {

	private List<Tag> tag;

	public List<Tag> getTags() {
		return tag;
	}
	public List<Tag> getTag() {
		return tag;
	}

	public void setTags(List<Tag> tag) {
		this.tag = tag;
	}
	
	public void setTag(List<Tag> tag) {
		this.tag = tag;
	}
	
	public String toString() {
    	ObjectMapper mapper = new ObjectMapper();
    	
    	String jsonString = "";
		try {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			jsonString = mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
    	return jsonString;
    }
	
}
