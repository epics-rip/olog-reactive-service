package edu.msu.frib.daolog.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import edu.msu.frib.daolog.log.Tag;
import edu.msu.frib.daolog.log.TagWrapper;
import edu.msu.frib.daolog.repository.TagDBUtils;
import edu.msu.frib.daolog.repository.TagRepository;

@Controller
public class TagController {
	private static Logger logger = LoggerFactory.getLogger(TagController.class);
	
	@Autowired
	private TagRepository tagRepository;

	/**
	 * Create new tag provided in request body as JSON
	 * 
	 * @param tag
	 * @return
	 */
	@PostMapping("/daolog/resources/tag/createByJSON")
	@ResponseBody
	public Tag testCreateTagJSON(@RequestBody Tag tag) {

		tag.setCreatedDate(new Date());
		logger.info("creating new tag " + tag.getName());
		Tag savedTag = tagRepository.save(tag);
		logger.info("new tag: " + tag);
		return savedTag;
	}

	@PostMapping("/daolog/resources/tags/createByJSON")
	@ResponseBody
	public Set<Tag> createTags(InputStream tagsStream) throws JsonParseException, JsonMappingException, IOException {
		
		// call MongoDB to insert them all into the database
		Set<Tag> savedTags = TagDBUtils.insertTags(tagRepository, tagsStream);		
		return savedTags; 
	}

	/**
	 * Get All tags in the database
	 * 
	 * @return
	 */
	@GetMapping("/daolog/resources/tags")
	@ResponseBody
	public Set<Tag> getTags() {

		logger.info("findAll() tags from mongodb!");
		return TagDBUtils.findTags(tagRepository);

	}

	/**
	 * Get all tags submitted in the request body as JSON list
	 * 
	 * @param tagIds
	 * @return
	 */
	@PostMapping("/daolog/resources/tags/ids")
	@ResponseBody
	public Set<Tag> getTagsById(@RequestBody List<String> tagIds) {

		logger.info("findAll(List<String>) tagIds.forEach() => ");
		tagIds.forEach(tagId -> logger.info(tagId));
		return TagDBUtils.findTags(tagRepository, tagIds);

	}

	/**
	 * Get all tags submitted in the request body as JSON list
	 * 
	 * @param tagIds
	 * @return
	 */
	@PostMapping("/daolog/resources/tags")
	@ResponseBody
	public Set<Tag> createTags(@RequestBody TagWrapper wrapper) {

		if (wrapper == null) throw new IllegalArgumentException("POST:/daolog/resources/tags Tags must be specified for this request");
		
		if (logger.isTraceEnabled()) logger.trace("createTags: {}", wrapper);

		String currentPrincipalName = SecurityContextHolder.getContext().getAuthentication().getName();
		
		Set<Tag> savedTags = new HashSet<Tag>();
		
		for (Tag tag : wrapper.getTags()) {

			tag.setCreatedDate(new Date());
			tag.setOwner(currentPrincipalName);
			if (logger.isTraceEnabled()) logger.trace("creating new tag " + tag.getName());
			Tag savedTag = tagRepository.save(tag);
			if (logger.isDebugEnabled()) logger.debug("new tag: " + tag);
			savedTags.add(savedTag);
		}
		
		return savedTags;

	}

	/**
	 * Get a specific tag provided on the URL
	 * 
	 * @param tagId
	 * @return
	 */
	@GetMapping("/daolog/resources/tag/{id}")
	@ResponseBody
	public Tag getTagById(@PathVariable("id") String tagId) {

		logger.info("find() tag_id from mongodb!");
		return TagDBUtils.findTag(tagRepository, tagId);

	}

	@GetMapping("/daolog/delete-tags")
	@ResponseBody
	public void deleteAllTags() throws IOException {

		logger.info("delete all tag documents from mongodb");
		tagRepository.deleteAll();

	}
}
