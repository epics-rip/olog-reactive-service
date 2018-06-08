package edu.msu.frib.daolog.security;

import static edu.msu.frib.daolog.security.SecurityConstants.HEADER_STRING;
import static edu.msu.frib.daolog.security.SecurityConstants.TOKEN_PREFIX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import edu.msu.frib.daolog.security.ApplicationUser;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * JWT Authentication filter
 * Please do NOT log the JWT secret or the user's password!
 * 
 * @author vagrant
 *
 */
public class JWTAuthnFilter extends UsernamePasswordAuthenticationFilter {	

	private static Logger logger = LoggerFactory.getLogger(JWTAuthnFilter.class);
		
	private Long expirationTime;
	private String secret;
	
    private AuthenticationManager authenticationManager;

    /**
     * Constructor to inject the authenticationManager
     * @param authenticationManager
     */
    public JWTAuthnFilter(AuthenticationManager authenticationManager, String secret, Long expirationTime) {
        this.authenticationManager = authenticationManager;
        this.secret = secret;
        this.expirationTime = expirationTime;
    }

    /**
     * Method passing credentials to Spring framework to perform authn against the 
     * authentication mechanism
     * 
     * TODO ought to use OAUTH2 to separate authentication concerns from the application
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req,
                                                HttpServletResponse res) throws AuthenticationException {
    	if (logger.isDebugEnabled()) logger.debug("inside attemptAuthentication...");
    	
    	String authHeader = req.getHeader("Authorization");
    	if (logger.isDebugEnabled()) logger.debug("userPass: {}", authHeader.split(" ")[1]);    	
    	Decoder decoder = Base64.getDecoder();    	
    	String userPass = new String(decoder.decode(authHeader.split(" ")[1].getBytes()));
    	
        ApplicationUser creds = new ApplicationUser();
        creds.setUsername(userPass.split(":")[0]);
        creds.setPassword(userPass.split(":")[1]);

        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        creds.getUsername(),
                        creds.getPassword(),
                        new ArrayList<>())
        );
        
    }

    /**
     * Method generating JWT string upon successful authentication.
     * It also pulls authorities from the authentication mechanism to encode with the JWT
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {

    	if (logger.isDebugEnabled()) {
    		auth.getAuthorities().forEach(authority -> logger.debug("authority: {}", authority.getAuthority()));
    		logger.info("Expiration Time: {}", expirationTime);
    	}
    	
        String token = Jwts.builder()
                .setSubject(((UserDetails) auth.getPrincipal()).getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .claim("authorities", auth.getAuthorities())
                .signWith(SignatureAlgorithm.HS512, secret.getBytes())
                .compact();
        res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
    }

	public Long getExpirationTime() {
		return expirationTime;
	}

	public void setExpirationTime(Long expirationTime) {
		this.expirationTime = expirationTime;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
}
