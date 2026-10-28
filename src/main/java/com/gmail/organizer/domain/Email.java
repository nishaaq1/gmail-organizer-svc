package com.gmail.organizer.domain;

public record Email(
        String id,
        String threadId,
        String from,
        String subject,
        String date,
        String snippet) {}
