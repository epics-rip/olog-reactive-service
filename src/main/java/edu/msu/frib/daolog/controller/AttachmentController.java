package edu.msu.frib.daolog.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mongodb.MongoGridFSException;
import com.mongodb.client.gridfs.model.GridFSFile;

import edu.msu.frib.daolog.StorageFileNotFoundException;
import edu.msu.frib.daolog.log.Log;
import edu.msu.frib.daolog.log.attachment.LogAttachmentStorageService;
import edu.msu.frib.daolog.repository.LogRepository;
import net.coobird.thumbnailator.Thumbnails;

import static edu.msu.frib.daolog.ApplicationConstants.ATTACHMENT_SEPARATOR;
import static edu.msu.frib.daolog.ApplicationConstants.MAX_ATTACHMENT_SIZE_BYTES;
import static edu.msu.frib.daolog.ApplicationConstants.THUMBNAIL_WIDTH_PIXELS;;

@Controller
@RequestMapping("/daolog/resources/attachments/")
public class AttachmentController {

    Logger logger = LoggerFactory.getLogger(AttachmentController.class);
    
    private final LogAttachmentStorageService logAttachmentStorageService;

	@Autowired
	private LogRepository logRepository;
	
	@Autowired
	private GridFsOperations gridOperations;
		
    @Autowired
    public AttachmentController(LogAttachmentStorageService logAttachmentStorageService) {
        this.logAttachmentStorageService = logAttachmentStorageService;
    }
    
    @GetMapping("{logId}/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> readAttachment(@PathVariable String logId, @PathVariable String filename) {
    	
    	logger.info("logId: {}; filename: {}", logId, filename);
    	    	  	
    	Resource resource = gridOperations.getResource(filename);
    	
    	if (logger.isDebugEnabled()) logger.debug("resource == " + resource);
    	if (resource != null) {
    		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    	} else {
    		resource = null;
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resource);
    	}
    }    

    @GetMapping("{logId}/{filename:.+}:thumbnail")
    @ResponseBody
    public ResponseEntity<Resource> getThumbnail(@PathVariable String logId, @PathVariable String filename) {
    	
    	if (logger.isDebugEnabled()) logger.debug("logId: {}; filename: {}", logId, filename);
    	String thumbnailName = (filename.contains(":thumbnail")) ? filename : filename + ":thumbnail";
    	    	  	
    	GridFSFile thumbnailAttachment = gridOperations.findOne(new Query(Criteria.where("filename").is(thumbnailName)));
    	GridFSFile attachment = gridOperations.findOne(new Query(Criteria.where("filename").is(filename)));
    	
    	
    	Resource resource = gridOperations.getResource(thumbnailName);
    	
    	// get the contentType
    	String contentType = null;
    	Document metadata = thumbnailAttachment.getMetadata();
    	// if it's an old style gridFsFile...
    	if (metadata != null) {
    		if (metadata.get("_contentType") != null) {
    			contentType = metadata.get("_contentType").toString();
    		}
    	}
    		
    	if (contentType == null || contentType.isEmpty()) {
    		try {
    			contentType = thumbnailAttachment.getContentType();
    		} catch (MongoGridFSException e) {
    			// suppress the exception
    			logger.warn(e.getMessage());
    		}
    	}
    	
    	logger.info("resource == {}", resource);
    	if (resource != null) {
    		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
    				.header(HttpHeaders.CONTENT_TYPE, contentType)
    				.header("X-OLOG-ATTACHMENT-ID", attachment.getId().toString())
    				.body(resource);
    	} else {
    		resource = null;
        	logger.info("thumbnail resource is null");
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resource);
    	}
    }

    @PostMapping("{logId}")
	@PreAuthorize("hasAuthority('ROLE_OLOG-LOGS')")
    public ResponseEntity<Resource> uploadAttachment(@PathVariable String logId, @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

    	if(logger.isDebugEnabled()) {
    		logger.trace("entering uploadAttachment method...");
    		logger.debug("storing file: " + file.getName() +" " + file.getSize() + " " + file.getContentType());    	
    	}
    	
    	ObjectId attachment = null;
    	ObjectId thumbnail = null;
    	
		try ( InputStream fileInputStream = file.getInputStream() ) {
			BufferedInputStream buffStream = new BufferedInputStream(fileInputStream);
			buffStream.mark(MAX_ATTACHMENT_SIZE_BYTES);
			attachment = gridOperations.store(buffStream, logId+ATTACHMENT_SEPARATOR+file.getOriginalFilename(), file.getContentType());			

	    	/* create thumbnail as separate document
	    	   resize to a specific size for ease of retrieval
	    	   maintain image ratio */
			buffStream.reset();
			if (file.getContentType().contains("image")) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                
				Thumbnails.of(buffStream)
					.height(THUMBNAIL_WIDTH_PIXELS)
					.keepAspectRatio(true)
					.outputFormat(file.getContentType().split("/")[1])
					.toOutputStream(outputStream);	
				
				thumbnail = gridOperations
						.store(new ByteArrayInputStream(outputStream.toByteArray()), 
								logId+ATTACHMENT_SEPARATOR+file.getOriginalFilename()+":thumbnail", 
								file.getContentType());
			}
		} catch (IOException e) {
			logger.warn("IOException thrown: " + e.getMessage());
			e.printStackTrace();
		}    	
    	
    	logger.info("finished storing the file " + file.getName());
    	
    	// Store the attachmentId with the log entry
    	// System enabled edit handling saving the attachmentIds to the log entry
    	Optional<Log> log = logRepository.findById(logId);
    	List<String> attachmentIds = log.get().getAttachmentIds();
    	attachmentIds.add(attachment.toString());
    	log.get().setAttachmentIds(attachmentIds);
    	logRepository.save(log.get());    	
    	
    	// TODO: submit document location to ElasticSearch
    	// TODO: store the document in GIT (default)    	
    	
        redirectAttributes.addFlashAttribute("message",
                "successfully uploaded '" + file.getOriginalFilename());

        //return "redirect:/";
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    
    @PutMapping("{logId}/{filename:.+}")
    @ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED, reason="Changing attachments of existing log entries is not allowed.")
    public void putAttachment() {
    	logger.warn("PUT attachments not implemented.");
    }
    
    @DeleteMapping("{logId}/{filename:.+}")
	@PreAuthorize("hasAuthority('ROLE_OLOG-ADMIN')")
    public ResponseEntity<Resource> deleteAttachment(@PathVariable String logId, @PathVariable String filename) {

    	logger.debug("removing attachment {} from log {}", filename, logId);

    	//return "redirect:/";

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
