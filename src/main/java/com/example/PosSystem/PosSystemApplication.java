package com.example.PosSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PosSystemApplication {

	public static void main(String[] args) {
		// FIX: Force Java to use IPv4.
		// This stops Google from silently dropping the connection and causing the 5000ms timeout!
		System.setProperty("java.net.preferIPv4Stack", "true");

		SpringApplication.run(PosSystemApplication.class, args);
	}

}