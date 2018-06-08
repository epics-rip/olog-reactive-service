package edu.msu.frib.daolog.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityUtils {
	
	private static Logger logger = LoggerFactory.getLogger(SecurityUtils.class);	
	
	public static String getUserDN(String userDNFormat, String user) {
		logger.info("userDNFormat: {}", userDNFormat);
		return userDNFormat.replace("{user}", user);
	}
}
