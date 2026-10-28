package com.gmail.organizer.config;

import com.gmail.organizer.infrastructure.gmail.GmailAuthorizationExchangeFilter;
import com.gmail.organizer.property.GmailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GmailWebClientConfig {
    @Bean
    WebClient gmailWebClient(GmailProperties properties,
                             GmailAuthorizationExchangeFilter authorizationFilter) {
        return WebClient.builder()
                .baseUrl(properties.api().baseUrl())
                .filter(authorizationFilter.bearerToken())
                .build();
    }
}
