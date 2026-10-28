package com.gmail.organizer.adapter;

import com.gmail.organizer.client.GmailApiClient;
import com.gmail.organizer.gateway.EmailGateway;
import com.gmail.organizer.dto.EmailPageResponse;
import org.springframework.stereotype.Component;

@Component
public class GmailApiAdapter implements EmailGateway {
    private final GmailApiClient gmailApiClient;

    public GmailApiAdapter(GmailApiClient gmailApiClient) {
        this.gmailApiClient = gmailApiClient;
    }

    @Override
    public EmailPageResponse getEmails(int pageSize, String pageToken) {
        return gmailApiClient.listEmails(pageSize, pageToken);
    }
}
