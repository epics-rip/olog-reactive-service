package edu.msu.frib.daolog.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/daolog")
public class DefaultController {

	private static Logger logger = LoggerFactory.getLogger(DefaultController.class);

	
	@GetMapping("/")
	public String home() {
		return "home";
	}
	
	@GetMapping("/status")
	@ResponseBody
	public String status() throws IOException {
		return "DAOLOG running ok";
	}

	@GetMapping("/monitor")
	public String redirectToMonitor() throws IOException {
		return "redirect:http://localhost:2812";
	}

}
