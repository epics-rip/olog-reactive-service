package edu.msu.frib.daolog.log.attachment;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import edu.msu.frib.daolog.log.Attachment;
import edu.msu.frib.daolog.log.Log;
import edu.msu.frib.daolog.repository.AttachmentRepository;
import edu.msu.frib.daolog.repository.LogRepository;

@Component
public class LogAttachmentStorageServiceImpl implements LogAttachmentStorageService {
	
	Logger logger = LoggerFactory.getLogger(LogAttachmentStorageServiceImpl.class);

	@Autowired
	private AttachmentRepository attachmentRepository;
	
	@Override
	public void init() {
		// Nothing to init
		
	}

	@Override
	public String store(MultipartFile file) {
		// Store the file in MongoDB
		// Obtain the BSON value, and provide that with the log message for storage
		// Then on retrieval, get the attachmentBSON value, and allow the browse to retrieve that as well...
		
		// first, store it
		// https://www.mkyong.com/mongodb/spring-data-mongodb-save-binary-file-gridfs-example/
		logger.info("about to store multipart file " + file.getName());

    	Attachment msg = new Attachment.Builder(file)
    			.setFilename(file.getName())
    			.setFileMetadataDescription("more description")
    			.build();
    	    	
    	logger.info("created attachment, attempting to save it in mongodb...");
    	// save the message
    	Attachment storedAttachment = attachmentRepository.save(msg);
    	logger.info("attachment has been stored properly..." + storedAttachment.getId());
    	
    	// get the BSON ID value
    	return storedAttachment.getId();
	}

	@Override
	public Stream<Path> loadAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path load(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Resource loadAsResource(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		
	}

}
