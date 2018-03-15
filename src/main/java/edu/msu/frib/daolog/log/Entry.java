package edu.msu.frib.daolog.log;

import java.sql.Date;
import java.util.Collection;
import java.util.LinkedList;

import org.springframework.data.annotation.Id;

/**
*
* @author berryman
*/
public class Entry {

	@Id
    private Long id;
    
    private Date createdDate;    
    private State state = State.Active;
        
    //private Collection<BitemporalLog> logs = new LinkedList<BitemporalLog>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

    
    
}
