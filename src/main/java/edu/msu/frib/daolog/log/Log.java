package edu.msu.frib.daolog.log;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
*
* @author berryman
* @author carrivea
*/
@Document(collection="logs")
public class Log {
	@Id
    private String id;

    private String description;
    private String owner;
    private String source;
    private List<String> logbookIds = new LinkedList<String>();
    private State state;
    private Date createdDate;

    private List<String> tagIds = new LinkedList<String>();
    
    private Level level;
    private Integer version;
    private Date eventDate;
    private List<String> attachmentIds = new LinkedList<String>();
    
    // many-to-many relationships; populated only on RESTful reads
    private Set<Tag> tags = new HashSet<Tag>();
    private Set<Logbook> logbooks = new HashSet<Logbook>();
    private Set<Property> properties = new HashSet<Property>();
    private Set<LogAttribute> attributes = new HashSet<LogAttribute>();
    private String entry;
    private Set<Attachment> attachments = new HashSet<Attachment>();
    
    public Log() {
    	// Nothing to do; default dummy constructor, the way Spring likes 'em
    }
    
    public Log(String description, String owner, String source, List<String> logbookIds, State state, Date createdDate) {
    	this.description = description;
    	this.owner = owner;
    	this.source = source;
    	this.logbookIds = logbookIds;
    	this.state = state;
    	this.createdDate = createdDate;
    }
    
    public static class Builder {

        private String description;
        private String owner;
        private String source;
        private List<String> logbookIds;
        private State state;
        private Date createdDate;
        
        private List<String> tagIds;
        private Level level;
        private Integer version;
        private Date eventDate;
        private List<String> attachmentIds;
        
        public Builder(String description, String owner, String source, List<String> logbookIds, State state, Date createdDate) {
        	this.description = description;
        	this.owner = owner;
        	this.source = source;
        	this.logbookIds = logbookIds;
        	this.state = state;
        	this.createdDate = createdDate;
        }
        
        public Builder setLevel(Level level) {
        	this.level = level;
        	return this;
        }
        
        public Builder setVersion(Integer version) {
        	this.version = version;
        	return this;
        }
        
        public Builder setModifiedDate(Date eventDate) {
        	this.eventDate = eventDate;
        	return this;
        }
        
        public Builder setAttachmentIds(List<String> attachmentIds) {
        	this.attachmentIds = attachmentIds;
        	return this;
        }
        
        public Builder setTagId(String tag) {
        	tagIds.add(tag);
        	return this;
        }
        
        public Builder setLogbookId(String logbook) {
        	logbookIds.add(logbook);
        	return this;
        }
        
        public Log build() {
        	Log log = new Log(description, owner, source, logbookIds, state, createdDate);
        	return log;
        }
    }
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public Level getLevel() {
		return level;
	}
	public void setLevel(Level level) {
		this.level = level;
	}
	public State getState() {
		return state;
	}
	public void setState(State state) {
		this.state = state;
	}
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Set<LogAttribute> getAttributes() {
		return attributes;
	}
	public void setAttributes(Set<LogAttribute> attributes) {
		this.attributes = attributes;
	}
	public List<String> getTagIds() {
		return tagIds;
	}
	public void setTagIds(List<String> tagIds) {
		this.tagIds = tagIds;
	}
	public String getEntry() {
		return entry;
	}
	public void setEntry(String entry) {
		this.entry = entry;
	}
	public List<String> getLogbookIds() {
		return logbookIds;
	}
	public void setLogbookIds(List<String> logbookIds) {
		this.logbookIds = logbookIds;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public List<String> getAttachmentIds() {
		return attachmentIds;
	}
	public void setAttachmentIds(List<String> attachmentIds) {
		this.attachmentIds = attachmentIds;
	}

	public Set<Tag> getTags() {
		return tags;
	}

	public void setTags(Set<Tag> tags) {
		this.tags = tags;
	}

	public Set<Logbook> getLogbooks() {
		return logbooks;
	}

	public void setLogbooks(Set<Logbook> logbooks) {
		this.logbooks = logbooks;
	}

	public Set<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(Set<Attachment> attachments) {
		this.attachments = attachments;
	}

	public Set<Property> getProperties() {
		return properties;
	}

	public void setProperties(Set<Property> properties) {
		this.properties = properties;
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
