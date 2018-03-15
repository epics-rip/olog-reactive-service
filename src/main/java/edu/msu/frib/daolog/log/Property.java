package edu.msu.frib.daolog.log;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
*
* @author berryman
*/
@Document(collection="properties")
public class Property {

	@Id
    private Long id;
	
    private String name = null;
    private State state;
    
    //OneToMany(mappedBy = "property")
    private Set<Attribute> attributes = new HashSet<Attribute>();

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

	public Set<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Set<Attribute> attributes) {
		this.attributes = attributes;
	}
    
    
}
