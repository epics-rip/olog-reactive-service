package edu.msu.frib.daolog.log;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

@Document(collection="attachments")
public class Attachment {
	@Id
    private String id;
	
	private MultipartFile attachment;
	
	private String filename;	
	private String fileMetadataDescription;
	
	public Attachment(MultipartFile attachment) {
		this.attachment = attachment;
	}
	
	public static class Builder {

		private MultipartFile attachment;		
		private String filename;	
		private String fileMetadataDescription;
		
		public Builder(MultipartFile attachment) {
        	this.attachment = attachment;
        }
        
        public Builder setFilename(String filename) {
        	this.filename = filename;
        	return this;
        }
        
        public Builder setFileMetadataDescription(String fileMetadataDescription) {
        	this.fileMetadataDescription = fileMetadataDescription;
        	return this;
        }

        public Attachment build() {
        	Attachment attachment = new Attachment(this.attachment);
        	return attachment;
        }
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public MultipartFile getAttachment() {
		return attachment;
	}
	public void setAttachment(MultipartFile attachment) {
		this.attachment = attachment;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getFileMetadataDescription() {
		return fileMetadataDescription;
	}
	public void setFileMetadataDescription(String fileMetadataDescription) {
		this.fileMetadataDescription = fileMetadataDescription;
	}

}
