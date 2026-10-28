package com.gmail.organizer.dto;

import java.util.List;

public record EmailPageResponse(List<EmailResponse> emails, String nextPageToken, int resultCount) {
    public EmailPageResponse {
        emails = emails == null ? List.of() : List.copyOf(emails);
    }
}
