package com.hcl.services.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {
	
	@GetMapping("/hi")
	public ResponseEntity<String> getHelloWsh(){
		return ResponseEntity.ok("Hello Controller: Wishing..");
	}

}
