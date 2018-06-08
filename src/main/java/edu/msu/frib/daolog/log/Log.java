package edu.msu.frib.daolog.log;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
* Log entry
* 
* A log entry is one of a series of logs concerning a specific message at a specific moment in time.
* It may be appended, to correct other information, but the original will always be preserved.
* 
* Each log contains a created date, a unique database ID field, a description, an owner, and other information.
* 
* Log entries are associated with each other if they contain the same base bson ID in the entry field, and this ID is the original 
* parent entry.
* 
* createdDate is now the actual date this particular record was saved to the database, for parents and children.
* modifiedDate is derived, based on the latest createdDate of the entry series.
* 
* @author berryman
* @author carrivea
*/
@Document(collection="logs")
public class Log {
	
	private static Logger logger = LoggerFactory.getLogger(Log.class);
	
	@Id
    private String id;

    private Integer version;
    private String owner;
    private String source;
    private Level level;
    private State state;
    private String description;
    private Set<LogAttribute> attributes = new HashSet<LogAttribute>();
    private Set<Logbook> logbooks = new HashSet<Logbook>();
    private Set<Tag> tags = new HashSet<Tag>();
    private String entry;
    private String origin;
    
    private Date createdDate;
    private Date eventStart;
    
    private List<String> logbookIds = new LinkedList<String>();
    private List<String> tagIds = new LinkedList<String>();
    private List<String> attachmentIds = new LinkedList<String>();
    private List<String> propertyIds = new LinkedList<String>();
    
    // many-to-many relationships; populated only on RESTful reads
    private Set<Property> properties = new HashSet<Property>();
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
	public Date getEventStart() {
		return eventStart;
	}
	public void setEventStart(Date eventStart) {
		this.eventStart = eventStart;
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

	public void setTags(Collection<Tag> tags) {
		Set<Tag> tagSet = new HashSet<Tag>();
		tagSet.addAll(tags);
		this.tags = tagSet;
	}

	public Collection<Logbook> getLogbooks() {
		return logbooks;
	}

	public void setLogbooks(Collection<Logbook> logbooks) {
		if (logger.isTraceEnabled()) {
			logger.trace("setLogbooks(Set<Logbook> logbooks)...{}", logbooks);
			if (logbooks != null) {
				for (Logbook logbook : logbooks) {
					logger.trace("logbook: {}", logbook);
				}
			}
		}

		Set<Logbook> logbookSet = new HashSet<Logbook>();
		logbookSet.addAll(logbooks);
		this.logbooks = logbookSet;
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

	public void setProperties(Collection<Property> properties) {
		Set<Property> propertySet = new HashSet<Property>();
		propertySet.addAll(properties);
		this.properties = propertySet;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
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

	public List<String> getPropertyIds() {
		return propertyIds;
	}

	/**
	 * Pull the full fledged properties structure from the database as well
	 * @param propertyIds
	 */
	public void setPropertyIds(List<String> propertyIds) {
		this.propertyIds = propertyIds;
	}
}
