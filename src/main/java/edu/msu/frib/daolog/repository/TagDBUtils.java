package edu.msu.frib.daolog.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.msu.frib.daolog.log.Tag;
import edu.msu.frib.daolog.log.TagsWrapper;

public class TagDBUtils {
	
	private static Logger logger = LoggerFactory.getLogger(TagDBUtils.class);

	public static Tag findTag(TagRepository tagRepository, String tagId) {

        Optional<Tag> tag = tagRepository.findById(tagId);        
    	logging(tag.get(), "findAllTags(TagRepository tagRepository): ");
    	return tag.get();
	}
	
	/**
	 * Retrieve tag by tag name.
	 * TagName is unique in the database
	 * 
	 * @param tagRepository
	 * @param tagName
	 * @return
	 */
	public static Tag findTagByName(TagRepository tagRepository, String tagName) {

        Set<Tag> tags = tagRepository.findByName(tagName);        
    	logging(tags, "findAllTags(TagRepository tagRepository): ");
    	
    	return tags.toArray(new Tag[]{})[0];
	}
	
	public static Set<Tag> findTags(TagRepository tagRepository) {

        List<Tag> listTags = tagRepository.findAll();
        
        Set<Tag> tags = new HashSet<Tag>();
        listTags.forEach(tag -> tags.add(tag));

    	logging(tags, "findAllTags(TagRepository tagRepository): ");
    	return tags;
	}
	
	// Method to transform sparse tag objects with just _ids to full-fledged tag objects
	public static Set<Tag> findTags(TagRepository tagRepository, Set<Tag> tags) {
		
        List<String> tag_ids = new LinkedList<String>();        
        Set<Tag> tagsProper = new HashSet<Tag>();
        
        tags.forEach(tag -> tag_ids.add(tag.getId()));

    	// Perform the query to return the Iterable result of all matching tag objects
    	// then migrates the objects into the HashSet to return
    	(tagRepository.findAllById(tag_ids)).forEach(tag -> tagsProper.add(tag));

    	logging(tags, "findTags(TagRepository tagRepository, Set<Tag> tags) ");
        return tagsProper;
	}
	
	// Method to transform list of tag ids full-fledged tag objects
	public static Set<Tag> findTags(TagRepository tagRepository, List<String> tagList) {
	      
        Set<Tag> tags = new HashSet<Tag>();
        
    	// Perform the query to return the Iterable result of all matching tag objects
    	// then migrates the objects into the HashSet to return
    	(tagRepository.findAllById(tagList)).forEach(tag -> tags.add(tag));
    	
    	logging(tags, "findTags(TagRepository tagRepository, List<String> tagList) ");
        
        return tags;
	}
	
	public static Set<Tag> insertTags(TagRepository tagRepository, InputStream tagsStream) 
			throws JsonParseException, JsonMappingException, IOException {
		
		ObjectMapper mapper = new ObjectMapper();

		TagsWrapper tagsWrapper = new TagsWrapper();
			
		// convert into a TagsWrapper object
		tagsWrapper = mapper.readValue(tagsStream, TagsWrapper.class);	

		if (tagRepository == null) {
			logger.info("tagRepository is null!");
		}
		if (tagsWrapper == null) {
			logger.info("tagsWrapper is null!");
		}
		
        List<Tag> tagsList = tagRepository.insert(tagsWrapper.getTags());

        Set<Tag> tagsSet = new HashSet<Tag>(tagsList);
        
        return tagsSet;
	}

	public static Set<Tag> insertTags(MongoTemplate mongoTemplate, InputStream tagsStream) 
			throws JsonParseException, JsonMappingException, IOException {
		
		ObjectMapper mapper = new ObjectMapper();

		TagsWrapper tagsWrapper = new TagsWrapper();
			
		// convert into a TagsWrapper object
		tagsWrapper = mapper.readValue(tagsStream, TagsWrapper.class);	

		if (mongoTemplate == null) {
			logger.info("mongoTemplate is null!");
		}
		if (tagsWrapper == null) {
			logger.info("tagsWrapper is null!");
		}
		
        mongoTemplate.insertAll(tagsWrapper.getTags());        
        List<Tag> tagsList = mongoTemplate.findAll(Tag.class);

        Set<Tag> tagsSet = new HashSet<Tag>(tagsList);
        
        return tagsSet;
	}
	
	public static void logging(Tag tag, String info) {
		Set<Tag> tags = new HashSet<Tag>();
		tags.add(tag);
		logging(tags, info);
	}
	
	public static void logging(Set<Tag> tags, String info) {

        StringBuffer buff = new StringBuffer();
        buff.append(info);
        buff.append("tag count: " + tags.size() + " ");
        
        for (Tag tag : tags) {
        	buff.append("\nTAG: ");
        	buff.append("name: " + tag.getName());
        	buff.append("owner: " + tag.getOwner());
        	buff.append("\n");
        }

        if (logger.isTraceEnabled()) logger.trace(buff.toString());
	}
}
