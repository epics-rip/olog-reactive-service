package edu.msu.frib.daolog.log;

import java.util.List;

/**
 * TODO
 * 
 * This object was created to handle an unfortunate incident in the logbook html, which creates
 * a logbook array in a JSON object like this:
 * 
 * { "logbook":[{"name":"namevalue","owner":"ownername","state":"Active"}]}
 * 
 * This does not mesh well with the naming convention I adopted for lists of objects: vis. an array of logbooks shoudl be called "logbooks"
 * 
 * { "logbooks":[{"name":"namevalue","owner":"ownername","state":"Active"}]}
 * 
 * Therefore, this needs to be discussed, and hammered out.
 * 
 * LogbooksWrapper2 is a class to alternatively handle this situation when the actual JSON name 
 * does not match the expected semantic name, until I can either refactor the rest of the code 
 * or agree to alter the front-end JSON.  This is very much in flux.
 * 
 * TODO
 * 
 * @author vagrant
 *
 */
public class LogbookWrapper {

	private List<Logbook> logbook;

	public List<Logbook> getLogbook() {
		return logbook;
	}

	public void setLogbook(List<Logbook> logbook) {
		this.logbook = logbook;
	}
	
	
}
