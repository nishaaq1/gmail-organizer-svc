package com.gmail.organizer.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "gmail")
public record GmailProperties(Api api, OAuth oauth) {

    public record Api(String baseUrl, int defaultPageSize, int maxPageSize) {
    }

    public record OAuth(
            String clientId,
            String clientSecret,
            String clientSecretFile,
            String tokenDirectory,
            String userKey,
            int callbackPort,
            List<String> scopes) {
    }
}
