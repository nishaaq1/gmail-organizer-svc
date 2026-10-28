package com.gmail.organizer.client;

import com.gmail.organizer.property.GmailProperties;
import com.gmail.organizer.dto.EmailPageResponse;
import com.gmail.organizer.dto.EmailResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GmailApiClient {
    private static final Logger log = LoggerFactory.getLogger(GmailApiClient.class);
    private final WebClient webClient;
    private final GmailProperties properties;

    public GmailApiClient(@Qualifier("gmailWebClient") WebClient gmailWebClient,
                          GmailProperties properties) {
        this.webClient = gmailWebClient;
        this.properties = properties;
    }

    public EmailPageResponse listEmails(int pageSize, String pageToken) {
        int safePageSize = Math.min(Math.max(pageSize, 1), properties.api().maxPageSize());
        MessageList list = webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/users/me/messages")
                            .queryParam("maxResults", safePageSize)
                            .queryParam("q", "in:anywhere -in:trash");
                    if (pageToken != null && !pageToken.isBlank()) {
                        builder.queryParam("pageToken", pageToken);
                    }
                    return builder.build();
                })
                .retrieve()
                .bodyToMono(MessageList.class)
                .block();

        if (list == null || list.messages() == null || list.messages().isEmpty()) {
            return new EmailPageResponse(List.of(), list == null ? null : list.nextPageToken(), 0);
        }

        List<EmailResponse> emails = new ArrayList<>();
        for (MessageRef ref : list.messages()) {
            MessageDetails details = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/me/messages/{id}")
                            .queryParam("format", "metadata")
                            .queryParam("metadataHeaders", "From")
                            .queryParam("metadataHeaders", "Subject")
                            .queryParam("metadataHeaders", "Date")
                            .build(Map.of("id", ref.id())))
                    .retrieve()
                    .bodyToMono(MessageDetails.class)
                    .block();

            if (details == null) continue;
            Map<String, String> headers = new java.util.HashMap<>();
            if (details.payload() != null && details.payload().headers() != null) {
                details.payload().headers().forEach(h ->
                        headers.put(h.name().toLowerCase(), h.value()));
            }
            emails.add(new EmailResponse(details.id(), details.threadId(),
                    headers.get("from"), headers.get("subject"), headers.get("date"),
                    details.snippet()));
        }

        log.info("Fetched {} Gmail messages", emails.size());
        return new EmailPageResponse(emails, list.nextPageToken(), emails.size());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MessageList(List<MessageRef> messages,
                              @JsonProperty("nextPageToken") String nextPageToken) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MessageRef(String id, String threadId) {}



    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MessageDetails(String id, String threadId, String snippet, Payload payload) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payload(List<Header> headers) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(String name, String value) {}
}
