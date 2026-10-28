package com.gmail.organizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GmailOrganizerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmailOrganizerApplication.class, args);
    }
}
