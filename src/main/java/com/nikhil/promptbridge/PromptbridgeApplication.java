package com.nikhil.promptbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = { org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class })
public class PromptbridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PromptbridgeApplication.class, args);
	}

}
