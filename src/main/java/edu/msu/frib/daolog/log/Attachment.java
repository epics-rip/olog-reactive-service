package edu.msu.frib.daolog.log;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

@Document(collection="fs.files")
public class Attachment {
	@Id
    private String id;
	
	private String filename;
	private String contentType;
	private Integer chunkSize;
	private Integer length;
	private boolean thumbnail;
	private String owner;
	private org.bson.Document metadata;
	
	private MultipartFile attachment;
	
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
	public String getContentType() {
		return (getMetadata() != null) ? getMetadata().getString("_contentType") : contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public Integer getChunkSize() {
		return chunkSize;
	}
	public void setChunkSize(Integer chunkSize) {
		this.chunkSize = chunkSize;
	}
	public boolean isThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(boolean thumbnail) {
		this.thumbnail = thumbnail;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public Integer getLength() {
		return length;
	}
	public void setLength(Integer length) {
		this.length = length;
	}
	public org.bson.Document getMetadata() {
		return metadata;
	}
	public void setMetadata(org.bson.Document metadata) {
		this.metadata = metadata;
	}

}
