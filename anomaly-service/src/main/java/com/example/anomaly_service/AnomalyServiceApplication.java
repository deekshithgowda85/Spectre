package com.example.anomaly_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AnomalyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnomalyServiceApplication.class, args);
	}

}
