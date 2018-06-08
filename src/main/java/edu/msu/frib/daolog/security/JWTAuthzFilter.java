package edu.msu.frib.daolog.security;

import static edu.msu.frib.daolog.security.SecurityConstants.HEADER_STRING;
import static edu.msu.frib.daolog.security.SecurityConstants.TOKEN_PREFIX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.ldap.userdetails.LdapAuthority;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

/**
 * Filters authorization for Olog.  Will determine whether the token is still (or ever was) valid.
 * It will throw a 401 or a 403 for invalid requests
 * @author vagrant
 *
 */
public class JWTAuthzFilter extends BasicAuthenticationFilter {
	

	private static Logger logger = LoggerFactory.getLogger(JWTAuthzFilter.class);

	private String secret;	
	private String userDNFormat;
		
	/**
	 * Constructor
	 * @param authManager
	 */
    public JWTAuthzFilter(AuthenticationManager authManager, String secret, String userDNFormat) {
        super(authManager);
        this.secret = secret;
        this.userDNFormat = userDNFormat;
    }

    /**
     * Takes the Bearer header and calls a method to take the JWT to verify the signature and obtain the claims
     * Once this is complete, spring security will handle the rest
     * It then passes the filter down to any subsequent filters in the chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        try {
        	UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
        	SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException expired) {
        	logger.info("JWT expired");
        	res.setStatus(HttpStatus.SC_UNAUTHORIZED);
        } 
        
        chain.doFilter(req, res);
    }

    /**
     * Private method to 
     * @param request
     * @return
     */
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) throws ExpiredJwtException {
        String token = request.getHeader(HEADER_STRING);
        try {
	        if (token != null) {
	            // parse the token.
	            String user = Jwts.parser()
	                    .setSigningKey(secret.getBytes())
	                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
	                    .getBody()
	                    .getSubject();
	            
	
	            Map<String, Object> claims = Jwts.parser()
	                    .setSigningKey(secret.getBytes())
	                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
	                    .getBody();
	            	            
	           @SuppressWarnings("unchecked")
				List<LinkedHashMap<String, String>> authorityList = (List<LinkedHashMap<String, String>>)claims.get("authorities");
	            
	            List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
	            if (logger.isDebugEnabled()) {
	            	authorityList.forEach(map -> map.values().forEach(value -> logger.debug("role: {}", value)));
	            }
	            authorityList.forEach(map -> map.values().forEach(value -> roles.add(new LdapAuthority(value.toString(), SecurityUtils.getUserDN(userDNFormat, user)))));
	            
	
	            if (user != null) {
	                return new UsernamePasswordAuthenticationToken(user, null, roles);
	            }
	            return null;
	        }
	    } catch (SignatureException se) {
	    	logger.error("SignatureException: someone was trying to manipulate the JWT.\nIP-Address:{}\nToken: {}\n\n\n", request.getLocalAddr(), token);
	    	logger.error("{}", se.toString());
	    	se.printStackTrace();
        	throw new RuntimeException("Invalid request.  Please contact controlshelp@frib.msu.edu.");
        }
        
        return null;
    }
}
