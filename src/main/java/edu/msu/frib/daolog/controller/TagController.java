package edu.msu.frib.daolog.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

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
	 * Get All tags in the database
	 * 
	 * @return
	 */
	@GetMapping("/daolog/resources/tags")
	@ResponseBody
	public TagWrapper getTags() {

		logger.info("findAll() tags from mongodb!");
		
		TagWrapper wrapper = new TagWrapper();
		Collection<Tag> tagCollection = TagDBUtils.findTags(tagRepository);
		List<Tag> tagList = new ArrayList<Tag>();
		tagList.addAll(tagCollection);
		
		wrapper.setTag(tagList);		
		return wrapper;

	}
	
	/**
	 * Get all tags submitted in the request body as JSON list
	 * 
	 * @param tagIds
	 * @return
	 */
	@PostMapping("/daolog/resources/tags/ids")
	@PreAuthorize("hasAuthority('ROLE_OLOG-TAGS')")
	@ResponseBody
	public Set<Tag> getTagsById(@RequestBody List<String> tagIds) {

		logger.info("findAll(List<String>) tagIds.forEach() => ");
		tagIds.forEach(tagId -> logger.info(tagId));
		return TagDBUtils.findTags(tagRepository, tagIds);

	}

	/**
	 * Create multiple tags based on incoming JSON objects
	 * Get all tags submitted in the request body as JSON list
	 * 
	 * Jackson will convert the incident JSON object into a TagWrapper object
	 * 
	 * @param TagWrapper
	 * @return Set<Tag> the set of all Tags created
	 */
	@PostMapping("/daolog/resources/tags")
	@PreAuthorize("hasAuthority('ROLE_OLOG-TAGS')")
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
	 * Get a specific tag by name provided on the URL
	 * 
	 * @param tagId
	 * @return
	 */
	@GetMapping("/daolog/resources/tag/{tagName}")
	@ResponseBody
	public Tag getTagByName(@PathVariable String tagName) {

		logger.info("find() tag_id from mongodb!");
		return TagDBUtils.findTagByName(tagRepository, tagName);

	}
	
	/**
	 * Get a specific tag by id provided on the URL
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

	@DeleteMapping("/daolog/resources/tags/name/{tagName}")
	@PreAuthorize("hasAuthority('ROLE_OLOG-ADMINS')")
	@ResponseBody
	public void deleteTagByName(@PathVariable String tagName) throws IOException {

		logger.info("delete all tag documents from mongodb");
		Tag tag = getTagByName(tagName);
		tagRepository.delete(tag);

	}
	
	@DeleteMapping("/daolog/resources/tags/id/{tagId}")
	@PreAuthorize("hasAuthority('ROLE_OLOG-ADMINS')")
	@ResponseBody
	public void deleteTagById(@PathVariable String tagId) throws IOException {

		logger.info("delete all tag documents from mongodb");
		Optional<Tag> tag = tagRepository.findById(tagId);
		tagRepository.delete(tag.get());

	}
	
}
