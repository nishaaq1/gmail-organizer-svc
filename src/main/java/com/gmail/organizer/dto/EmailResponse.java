package com.gmail.organizer.dto;

public record EmailResponse(
        String id,
        String threadId,
        String from,
        String subject,
        String date,
        String snippet) {}
