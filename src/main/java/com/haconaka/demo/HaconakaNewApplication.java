package com.haconaka.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.haconaka")
public class HaconakaNewApplication {

	public static void main(String[] args) {
		SpringApplication.run(HaconakaNewApplication.class, args);
	}

}
