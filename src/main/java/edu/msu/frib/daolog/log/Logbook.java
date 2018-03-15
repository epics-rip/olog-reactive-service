package edu.msu.frib.daolog.log;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
*
* @author berryman
* @author carrivea
*/
@Document(collection="logbooks")
public class Logbook {

	@Id
    private String id = null;
	
    private String name = null;
    private String owner = null;
    private State state;
	private Date createdDate;
    
    public Logbook() {
    	// Nothing to do; default dummy constructor, the way Spring likes 'em
    }
    
    public Logbook(String name, String owner, State state) {
    	this.name = name;
    	this.owner = owner;
    	this.state = state;
    }
    
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
    
}
