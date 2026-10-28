package com.gmail.organizer.service;

import com.gmail.organizer.property.GmailProperties;
import com.gmail.organizer.dto.EmailPageResponse;
import com.gmail.organizer.gateway.EmailGateway;
import org.springframework.stereotype.Service;

@Service
public class EmailSearchService {
    private final EmailGateway emailGateway;
    private final GmailProperties properties;

    public EmailSearchService(EmailGateway emailGateway, GmailProperties properties) {
        this.emailGateway = emailGateway;
        this.properties = properties;
    }

    public EmailPageResponse getEmails(Integer pageSize, String pageToken) {
        int resolvedPageSize = pageSize == null
                ? properties.api().defaultPageSize() : pageSize;
        if (resolvedPageSize < 1 || resolvedPageSize > properties.api().maxPageSize()) {
            throw new IllegalArgumentException(
                    "pageSize must be between 1 and " + properties.api().maxPageSize());
        }
        return emailGateway.getEmails(resolvedPageSize, pageToken);
    }
}
