package edu.msu.frib.daolog.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;


@EnableWebSecurity(debug = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/**").permitAll()
				.antMatchers(HttpMethod.HEAD, "/**").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.antMatchers(HttpMethod.POST, "/daolog/resources/logs/**").hasRole("OLOG-LOGS")
				.antMatchers(HttpMethod.POST, "/daolog/resources/tags/**").hasRole("OLOG-TAGS")
				.antMatchers(HttpMethod.POST, "/daolog/resources/logbooks").hasRole("OLOG-LOGBOOKS")
				.antMatchers(HttpMethod.POST, "/daolog/resources/logbooks/**").hasRole("OLOG-LOGBOOKS")
				.antMatchers(HttpMethod.DELETE, "/**").hasRole("OLOG-ADMINS")
				.antMatchers(HttpMethod.PUT, "/**").hasRole("OLOG-ADMINS")
				.antMatchers(HttpMethod.PATCH, "/**").hasRole("OLOG-ADMINS")
				.antMatchers(HttpMethod.TRACE, "/**").hasRole("OLOG-ADMINS")
				.anyRequest().authenticated()
				.and()
				.httpBasic()
				;	
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
