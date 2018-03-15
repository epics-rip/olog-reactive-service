package edu.msu.frib.daolog.controller;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

@Controller
public class ElasticSearchRestController {


	@RequestMapping("/daolog/search")
	public String home() {

		RestHighLevelClient client = new RestHighLevelClient(
		        RestClient.builder(
		                new HttpHost("localhost", 9200, "http"),
		                new HttpHost("localhost", 9201, "http")));
		
		SearchRequest searchRequest = new SearchRequest();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		searchRequest.source(searchSourceBuilder);
		
		SearchResponse esResponse;
		try {
			esResponse = client.search(searchRequest);
		} catch (IOException e) {
			e.printStackTrace();
			return "Hello there! '" + e.getMessage() + "'";
		}
		return "success '" + esResponse.toString() + "'";
	}

	// Add a new attachment via the command line
	@RequestMapping(value = "/addMore/{unit}/{subunit}/{index}",
					method = RequestMethod.PUT)
	public String addMore(@PathVariable(value="unit") String unit, 
			@PathVariable(value="subunit") String subunit, 
			@PathVariable(value="index") long index,
			@RequestBody String bodyValue) {

		HttpClient httpClient = HttpClient.newHttpClient();
		System.out.println("unit == " + unit);
		System.out.println("subunit == " + subunit);
		System.out.println("index == " + index);
		System.out.println("body == " + bodyValue);
				 
		try {
			HttpResponse esResponse = httpClient.send(HttpRequest
			.newBuilder(new java.net.URI("http://localhost:9200/" + unit + "/" + subunit + "/" + index))
			.PUT(HttpRequest.BodyProcessor.fromString(bodyValue))
			.header("Content-Type", "application/json")
			.build(), HttpResponse.BodyHandler.asString());

			return "completed " + esResponse.statusCode();
			
		} catch (URISyntaxException | IOException | InterruptedException e) {
			e.printStackTrace();
			return "Error! '" + e.getMessage() + "'";
		}
		
	}
	
	@PostMapping("/daolog/es/testCreate")
	public String add(
    		@RequestParam(value="owner", required=true) String owner,
    		@RequestParam(value="description", required=true) String description,
    	    		@RequestParam(value="logbook", required=true) String logbook,
    	    		@RequestParam(value="tag", required=false) String tag,
    	    		@RequestParam(value="property", required=false) String property,
    	    		@RequestParam(value="source", required=true) String source,
    	    		@RequestParam(value="state", required=true) String stateString,
    	    		@RequestParam(value="level", required=false) String level,
    	    		@RequestParam(value="version", required=false) String version) {
						
		HttpClient httpClient = HttpClient.newHttpClient();
		
		try {
			HttpRequest
			.newBuilder(new java.net.URI("http://localhost:9200/daolog/rocks/1"))
			.PUT(HttpRequest.BodyProcessor.fromString("{\n" + 
					"    \"user\" : \"carrivea\",\n" + 
					"    \"post_date\" : \"2017-12-07T08:11:00\",\n" + 
					"    \"message\" : \"trying out Elasticsearch\"\n" + 
					"}"))

			.headers("Content-Type", "application/json")
			.GET()
			.build();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "completed";
	}
}
