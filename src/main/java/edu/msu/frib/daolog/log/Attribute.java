package edu.msu.frib.daolog.log;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
*
* @author berryman
*/
@Document(collection="attributes")
public class Attribute {

	@Id
    private Long id;
	
    private String name = null;
    private State state;
    private Set<LogAttribute> logs;
    private Property property;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
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
	public Set<LogAttribute> getLogs() {
		return logs;
	}
	public void setLogs(Set<LogAttribute> logs) {
		this.logs = logs;
	}
	public Property getProperty() {
		return property;
	}
	public void setProperty(Property property) {
		this.property = property;
	}
}
