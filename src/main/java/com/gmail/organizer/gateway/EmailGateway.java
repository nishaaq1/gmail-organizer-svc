package com.gmail.organizer.gateway;

import com.gmail.organizer.domain.Email;
import com.gmail.organizer.dto.EmailPageResponse;

public interface EmailGateway {
    EmailPageResponse getEmails(int pageSize, String pageToken);
}
