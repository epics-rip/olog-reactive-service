package edu.msu.frib.daolog.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.msu.frib.daolog.db.MongoDBAttachmentConfig;
import edu.msu.frib.daolog.StorageFileNotFoundException;
import edu.msu.frib.daolog.log.attachment.LogAttachmentStorageService;

@Controller
public class AttachmentUploadController {

    Logger logger = LoggerFactory.getLogger(AttachmentUploadController.class);
    
    private final LogAttachmentStorageService logAttachmentStorageService;
    
    @Autowired
    public AttachmentUploadController(LogAttachmentStorageService logAttachmentStorageService) {
        this.logAttachmentStorageService = logAttachmentStorageService;
    }

    @GetMapping("/daolog/list")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", logAttachmentStorageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(AttachmentUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/daolog/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = logAttachmentStorageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
    
    @GetMapping("/daolog/uploadForm.html")
    public String handleUploadForm() {
    	return "uploadForm";
    }

    @PostMapping("/daolog/resources/uploadAttachment")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {

    	logger.debug("entering uploadAttachment method...");
    	// TODO: how best to configure this to permit users to define the binary storage mechanism?
    	// TODO: Spring configuration in a text file, and default to GIT if nothing else is specified
    	// TODO: prevent accidental startup using GIT default in (FTC and office network) production...how to accomplish that?
    	
    	logger.info("about to store the file " + file.getName() +" " + file.getSize() + " " + file.getContentType());
    	// TODO: store the document in a database
    	// TODO does not work: logAttachmentStorageService.store(file);
    	
    	ApplicationContext ctx = new AnnotationConfigApplicationContext(MongoDBAttachmentConfig.class);
    	GridFsOperations gridOperations = (GridFsOperations) ctx.getBean("gridFsTemplate");
    	
    	InputStream fileInputStream = null;
    	
		try {
			fileInputStream = file.getInputStream();
    		gridOperations.store(fileInputStream, file.getOriginalFilename());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    	
    	
    	logger.info("finished storing the file " + file.getName());

    	// TODO: submit document location to ElasticSearch
    	// TODO: store the document in GIT (default)
    	
    	
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded '" + file.getOriginalFilename() + "' !");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
