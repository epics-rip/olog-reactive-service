package edu.msu.frib.daolog.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DefaultController {

	private static Logger logger = LoggerFactory.getLogger(DefaultController.class);

	
	@GetMapping("/daolog")
	public String home() {
		return "home";
	}
	
	@GetMapping("/daolog/status")
	@ResponseBody
	public String status() throws IOException {
		return "DAOLOG running ok";
	}

	@GetMapping("/daolog/monitor")
	public String redirectToMonitor() throws IOException {
		return "redirect:http://localhost:2812";
	}

}
