package edu.msu.frib.daolog.log;

import java.util.Date;

import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
*
* @author berryman
* @author carrivea
*/
@Document(collection="tags")
public class Tag {

	@Id
    private String id;
    private String name;
    private State state;
    private String owner;
	private Date createdDate;
	
	public Tag() {
		// default constructor
	}
    
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Size(min=1, max=250)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	@Size(max=50)
    public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
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
