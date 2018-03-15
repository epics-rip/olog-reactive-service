package edu.msu.frib.daolog.log;

import org.springframework.data.annotation.Id;

/**
*
* @author berryman
*/
public class LogAttribute {


	@Id
    private Long id;
	
    private Long logId;
    private Long attributeId;
    private String value;
    private Long groupingNum;
    
    // many-to-one relationship
    private Log log;
    private Attribute attribute;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getLogId() {
		return logId;
	}
	public void setLogId(Long logId) {
		this.logId = logId;
	}
	public Long getAttributeId() {
		return attributeId;
	}
	public void setAttributeId(Long attributeId) {
		this.attributeId = attributeId;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Long getGroupingNum() {
		return groupingNum;
	}
	public void setGroupingNum(Long groupingNum) {
		this.groupingNum = groupingNum;
	}
	public Log getLog() {
		return log;
	}
	public void setLog(Log log) {
		this.log = log;
	}
	public Attribute getAttribute() {
		return attribute;
	}
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
    
    
}
