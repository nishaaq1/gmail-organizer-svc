# Gmail Organizer

Java 21 + Spring Boot starter for learning LLD patterns while calling the Gmail REST API with WebClient.

## Security first
- Never commit `credentials/`, `tokens/`, `.env`, or real OAuth secrets.
- Keep Gmail scope read-only while implementing list/search.
- Bulk cleanup is intentionally not implemented in this starter.
- OAuth consent screen must have your Google account added as a test user while the app is in Testing.

## Configuration
The `application.yml` reads secrets from environment variables. Set these in IntelliJ Run Configuration > Environment variables:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`

Download the OAuth Desktop app client JSON from Google Cloud and keep it at `credentials/credentials.json`. This starter uses that JSON for the existing installed-app OAuth flow; the environment variables are reserved for future deployment/configuration options and should not be confused with the client JSON file.

## Run
1. Place your downloaded OAuth client JSON at `credentials/credentials.json`.
2. Confirm `.gitignore` ignores `credentials/` and `tokens/`.
3. Run `GmailOrganizerApplication` from IntelliJ.
4. Call `GET http://localhost:8080/api/v1/emails?pageSize=10`.
5. On first request, complete Google's browser authorization. Subsequent calls reuse the local credential store where valid.

The first version returns message metadata (ID, thread ID, sender, subject, date, snippet). Gmail may return fewer messages than requested. `nextPageToken` indicates another page.

## LLD decisions
- `EmailGateway` is the application-facing abstraction.
- `GmailApiClient` is the Gmail REST adapter and uses WebClient.
- `GoogleTokenProvider` owns token lifecycle.
- `GmailAuthorizationExchangeFilter` adds the bearer token to outbound calls.
- `CorrelationIdFilter` adds a request ID to incoming HTTP requests and MDC logs.
- `ApiResponse<T>` and `GlobalExceptionHandler` standardize API output.
- Keyword/sender strategies are the next implementation step.
