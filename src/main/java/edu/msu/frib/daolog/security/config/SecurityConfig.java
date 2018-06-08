package edu.msu.frib.daolog.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import edu.msu.frib.daolog.security.JWTAuthnFilter;
import edu.msu.frib.daolog.security.JWTAuthzFilter;


@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

	@Value("${olog.security.jwt.expiration_time}")
	private Long expirationTime;
	@Value("${olog.security.jwt.secret}")
	private String secret;
	@Value("${olog.ldap.userDN.format}")
	public String userDNFormat;
	
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
				
		http.cors().and().csrf().disable()
    	.authorizeRequests()	
			.antMatchers(HttpMethod.POST, "/daolog/resources/login").permitAll()
    		.antMatchers(HttpMethod.GET, "/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilter(new JWTAuthnFilter(authenticationManager(), secret, expirationTime))
            .addFilter(new JWTAuthzFilter(authenticationManager(), secret, userDNFormat))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

	}

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		logger.debug("configureGlobal ");
		auth
		.ldapAuthentication()
			.contextSource()
				.url("ldap://localhost:389/dc=frib,dc=msu,dc=edu")
				.port(389)
			.and()
			.userSearchBase("ou=users,")
			.userDnPatterns("uid={0},ou=users")
			.groupSearchBase("ou=groups")
			.groupSearchFilter("(memberUid={1})")
			;

		logger.debug("configureGlobal " + auth.toString());
	}
	
}
