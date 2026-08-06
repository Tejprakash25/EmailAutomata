package com.emailautomata;

import com.emailautomata.core.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the EmailAutomata API.
 *
 * <p>Package layout is feature-first: cross-cutting concerns live under
 * {@code core}, and each product capability owns a vertical slice under
 * {@code feature}.</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class EmailautomataApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailautomataApiApplication.class, args);
	}
}