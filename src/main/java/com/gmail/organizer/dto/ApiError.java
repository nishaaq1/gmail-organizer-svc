package com.gmail.organizer.dto;

import java.util.List;

public record ApiError(String code, String message, List<String> details) {
    public ApiError {
        details = details == null ? List.of() : List.copyOf(details);
    }
}
