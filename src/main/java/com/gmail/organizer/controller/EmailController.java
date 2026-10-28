package com.gmail.organizer.controller;

import com.gmail.organizer.dto.ApiResponse;
import com.gmail.organizer.dto.EmailPageResponse;
import com.gmail.organizer.exception.CorrelationIdFilter;
import com.gmail.organizer.service.EmailSearchService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/emails")
public class EmailController {
    private final EmailSearchService emailSearchService;

    public EmailController(EmailSearchService emailSearchService) {
        this.emailSearchService = emailSearchService;
    }

    @GetMapping
    public ApiResponse<EmailPageResponse> getEmails(
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String pageToken,
            HttpServletRequest request) {
        Object uid = request.getAttribute(CorrelationIdFilter.REQUEST_UID_ATTRIBUTE);
        return ApiResponse.success(uid == null ? "unknown" : uid.toString(),
                emailSearchService.getEmails(pageSize, pageToken));
    }
}
