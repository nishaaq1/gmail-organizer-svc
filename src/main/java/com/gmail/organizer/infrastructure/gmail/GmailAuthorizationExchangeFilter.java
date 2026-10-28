package com.gmail.organizer.infrastructure.gmail;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

/**
 * Centralizes outbound OAuth headers for Gmail API calls.
 * This is distinct from the servlet filter that assigns our inbound request UID.
 */
@Component
public class GmailAuthorizationExchangeFilter {
    private final GoogleTokenProvider tokenProvider;

    public GmailAuthorizationExchangeFilter(GoogleTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public ExchangeFilterFunction bearerToken() {
        return (request, next) -> {
            String token = tokenProvider.getAccessToken();
            ClientRequest authenticated = ClientRequest.from(request)
                    .headers(headers -> headers.setBearerAuth(token))
                    .build();
            return next.exchange(authenticated);
        };
    }
}
