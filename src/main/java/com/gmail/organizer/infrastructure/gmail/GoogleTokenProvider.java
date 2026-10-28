package com.gmail.organizer.infrastructure.gmail;

import com.gmail.organizer.property.GmailProperties;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Component
public class GoogleTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(GoogleTokenProvider.class);
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final GmailProperties properties;
    private volatile Credential credential;

    public GoogleTokenProvider(GmailProperties properties) {
        this.properties = properties;
    }

    public String getAccessToken() {
        Credential current = getOrAuthorize();
        synchronized (this) {
            try {
                if (current.getAccessToken() == null
                        || current.getExpiresInSeconds() == null
                        || current.getExpiresInSeconds() < 60) {
                    boolean refreshed = current.refreshToken();
                    if (!refreshed || current.getAccessToken() == null) {
                        credential = null;
                        throw new IllegalStateException(
                                "Google token refresh failed. Re-authorize the application.");
                    }
                }
                return current.getAccessToken();
            } catch (IOException ex) {
                credential = null;
                throw new IllegalStateException("Unable to refresh Google OAuth token", ex);
            }
        }
    }

    private Credential getOrAuthorize() {
        Credential current = credential;
        if (current != null) return current;

        synchronized (this) {
            if (credential != null) return credential;
            try {
                File secretFile = new File(properties.oauth().clientSecretFile());
                if (!secretFile.isFile()) {
                    throw new IllegalStateException(
                            "OAuth client file not found: " + secretFile.getPath());
                }

                NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
                GoogleClientSecrets secrets;
                try (FileReader reader = new FileReader(secretFile)) {
                    secrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
                }

                List<String> scopes = properties.oauth().scopes();
                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                        transport, JSON_FACTORY, secrets, scopes)
                        .setDataStoreFactory(new FileDataStoreFactory(
                                new File(properties.oauth().tokenDirectory())))
                        .setAccessType("offline")
                        .build();

                LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                        .setPort(properties.oauth().callbackPort())
                        .build();

                log.info("Starting Google OAuth authorization if no valid local credential exists");
                credential = new AuthorizationCodeInstalledApp(flow, receiver)
                        .authorize(properties.oauth().userKey());
                return credential;
            } catch (IOException | GeneralSecurityException ex) {
                throw new IllegalStateException("Unable to initialize Google OAuth", ex);
            }
        }
    }
}
