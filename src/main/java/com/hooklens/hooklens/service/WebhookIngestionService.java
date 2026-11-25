
package com.hooklens.hooklens.service;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import com.hooklens.hooklens.entity.WebhookEndpoint;
import com.hooklens.hooklens.entity.WebhookEvent;

import com.hooklens.hooklens.repository.WebhookEndpointRepository;
import com.hooklens.hooklens.repository.WebhookEventRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WebhookIngestionService {

    private static final int MAX_BODY_SIZE = 1024 * 1024;

    private static final String REDACTED = "[REDACTED]";

    private static final String[] SENSITIVE_HEADERS = {
            "authorization",
            "proxy-authorization",
            "cookie",
            "set-cookie",
            "x-api-key",
            "x-access-token",
            "x-refresh-token",
            "x-client-secret",
            "x-webhook-secret",
            "x-csrf-token",
            "x-auth-token"
    };

    private final WebhookEndpointRepository endpointRepository;
    private final WebhookEventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public WebhookIngestionService(
            WebhookEndpointRepository endpointRepository,
            WebhookEventRepository eventRepository,
            PasswordEncoder passwordEncoder,
            ObjectMapper objectMapper
    ) {
        this.endpointRepository = endpointRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UUID ingestWebhook(
            String token,
            HttpServletRequest request,
            byte[] body
    ) {

        // 1. Validate request body size

        if (body == null) {
            body = new byte[0];
        }

        if (body.length > MAX_BODY_SIZE) {
            throw new IllegalArgumentException(
                    "Request body exceeds the 1 MB limit"
            );
        }

        // 2. Extract token prefix

        String prefix = extractTokenPrefix(token);

        // 3. Find endpoint using token prefix

        WebhookEndpoint endpoint = endpointRepository
                .findByTokenPrefix(prefix)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid webhook token"
                        )
                );

        // 4. Reject revoked endpoints

        if (endpoint.getRevokedAt() != null) {
            throw new IllegalArgumentException(
                    "Invalid webhook token"
            );
        }

        // 5. Verify complete token using BCrypt

        if (!passwordEncoder.matches(
                token,
                endpoint.getTokenHash()
        )) {
            throw new IllegalArgumentException(
                    "Invalid webhook token"
            );
        }

        // 6. Create WebhookEvent

        WebhookEvent event = new WebhookEvent();

        event.setEndpoint(endpoint);

        event.setHttpMethod(request.getMethod());

        event.setRequestHeaders(
                captureAndRedactHeaders(request)
        );

        event.setQueryParameters(
                redactQueryParameters(request.getQueryString())
        );
        event.setContentType(request.getContentType());

        event.setRawBody(body);

        event.setParsedJson(parseJsonBody(
                body,
                request.getContentType()
        ));

        // 7. Save event in PostgreSQL

        WebhookEvent savedEvent = eventRepository.save(event);

        // 8. Update endpoint's last received timestamp

        endpoint.setLastReceivedAt(LocalDateTime.now());

        // Both database changes commit together.

        return savedEvent.getId();
    }

    // Extract the 12-character prefix from the token

    private String extractTokenPrefix(String token) {

        if (token == null || !token.startsWith("hl_ep_")) {
            throw new IllegalArgumentException(
                    "Invalid webhook token"
            );
        }

        int separatorIndex = token.indexOf('_', 6);

        if (separatorIndex == -1) {
            throw new IllegalArgumentException(
                    "Invalid webhook token"
            );
        }

        String prefix = token.substring(6, separatorIndex);

        if (!prefix.matches("[0-9a-f]{12}")) {
            throw new IllegalArgumentException(
                    "Invalid webhook token"
            );
        }

        if (separatorIndex == token.length() - 1) {
            throw new IllegalArgumentException(
                    "Invalid webhook token"
            );
        }

        return prefix;
    }

    // Capture headers while redacting sensitive values

    private String captureAndRedactHeaders(
            HttpServletRequest request
    ) {

        Map<String, String> headers = new LinkedHashMap<>();

        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames == null) {
            return "{}";
        }

        while (headerNames.hasMoreElements()) {

            String headerName = headerNames.nextElement();

            if (isSensitiveHeader(headerName)) {

                headers.put(headerName, REDACTED);

            } else {

                headers.put(
                        headerName,
                        String.join(
                                ", ",
                                Collections.list(
                                        request.getHeaders(headerName)
                                )
                        )
                );
            }
        }

        try {
            return objectMapper.writeValueAsString(headers);
        } catch (JacksonException exception) {
            throw new IllegalStateException(
                    "Unable to capture request headers",
                    exception
            );
        }
    }

    // Check whether a header contains sensitive information

    private boolean isSensitiveHeader(String headerName) {

        for (String sensitiveHeader : SENSITIVE_HEADERS) {

            if (sensitiveHeader.equalsIgnoreCase(headerName)) {
                return true;
            }
        }

        return false;
    }

    // Parse JSON requests without losing the original body


    // Parse JSON requests without losing the original body
    private String parseJsonBody(
            byte[] body,
            String contentType
    ) {

        if (body.length == 0 || contentType == null) {
            return null;
        }

        // Remove optional parameters such as charset=UTF-8
        String normalizedContentType = contentType
                .split(";", 2)[0]
                .trim()
                .toLowerCase(Locale.ROOT);

        // Support application/json and application/*+json
        boolean isJsonContentType =
                normalizedContentType.equals("application/json")
                        || (
                        normalizedContentType.startsWith("application/")
                                && normalizedContentType.endsWith("+json")
                );

        if (!isJsonContentType) {
            return null;
        }

        try {

            objectMapper.readTree(
                    new String(body, StandardCharsets.UTF_8)
            );

            return new String(body, StandardCharsets.UTF_8);

        } catch (JacksonException exception) {

            // Preserve the original body even if JSON is malformed.
            return null;
        }
    }

    private String redactQueryParameters(String queryString) {

        if (queryString == null || queryString.isBlank()) {
            return queryString;
        }

        Set<String> sensitiveParameters = Set.of(
                "password",
                "passwd",
                "token",
                "accesstoken",
                "refreshtoken",
                "apikey",
                "clientsecret",
                "webhooksecret",
                "secret",
                "authorization",
                "signature",
                "sig"
        );

        String[] parameters = queryString.split("&", -1);

        for (int i = 0; i < parameters.length; i++) {

            String parameter = parameters[i];

            int separatorIndex = parameter.indexOf('=');

            if (separatorIndex == -1) {
                continue;
            }

            String parameterName = parameter.substring(
                    0, separatorIndex
            );

            String normalizedName;

            try {
                normalizedName = URLDecoder.decode(
                                parameterName,
                                StandardCharsets.UTF_8
                        ).toLowerCase(Locale.ROOT)
                        .replace("-", "")
                        .replace("_", "");

            } catch (IllegalArgumentException exception) {
                normalizedName = parameterName.toLowerCase(Locale.ROOT)
                        .replace("-", "")
                        .replace("_", "");
            }

            if (sensitiveParameters.contains(normalizedName)) {

                parameters[i] = parameterName + "=[REDACTED]";
            }
        }

        return String.join("&", parameters);

    }
}
