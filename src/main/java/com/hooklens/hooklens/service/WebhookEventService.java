
package com.hooklens.hooklens.service;

import java.util.Map;
import java.util.LinkedHashMap;
import com.hooklens.hooklens.dto.response.WebhookEventStatsResponse;

import com.hooklens.hooklens.service.JsonRedactionService;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.hooklens.hooklens.dto.WebhookEventResponse;
import com.hooklens.hooklens.entity.ApiKey;
import com.hooklens.hooklens.entity.WebhookEndpoint;
import com.hooklens.hooklens.entity.WebhookEvent;
import com.hooklens.hooklens.repository.ApiKeyRepository;
import com.hooklens.hooklens.repository.WebhookEndpointRepository;
import com.hooklens.hooklens.repository.WebhookEventRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class WebhookEventService {

    private final JsonRedactionService jsonRedactionService;
    private final ApiKeyRepository apiKeyRepository;
    private final WebhookEndpointRepository endpointRepository;
    private final WebhookEventRepository eventRepository;

    public WebhookEventService(
            ApiKeyRepository apiKeyRepository,
            WebhookEndpointRepository endpointRepository,
            WebhookEventRepository eventRepository,
            JsonRedactionService jsonRedactionService
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.endpointRepository = endpointRepository;
        this.eventRepository = eventRepository;
        this.jsonRedactionService = jsonRedactionService;
    }
    // Retrieve all events belonging to an endpoint

    @Transactional(readOnly = true)
    public List<WebhookEventResponse> getEventsForEndpoint(
            UUID endpointId
    ) {

        UUID userId = getAuthenticatedUserId();

        WebhookEndpoint endpoint = endpointRepository
                .findByIdAndUser_Id(endpointId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Webhook endpoint not found"
                ));

        List<WebhookEvent> events =
                eventRepository.findAllByEndpoint_IdOrderByReceivedAtDesc(
                        endpoint.getId()
                );

        return events.stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public Page<WebhookEventResponse> getPaginatedEvents(
            UUID endpointId,
            int page,
            int size,
            String method,
            LocalDateTime from,
            LocalDateTime to
    ) {

        // 1. Authenticate API key
        UUID userId = getAuthenticatedUserId();

        // 2. Verify endpoint ownership
        WebhookEndpoint endpoint = endpointRepository
                .findByIdAndUser_Id(endpointId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Webhook endpoint not found"
                ));

        // 3. Validate pagination
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100"
            );
        }

        // 4. Validate date range
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "'from' date cannot be after 'to' date"
            );
        }

        // 5. Create pagination
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "receivedAt")
        );

        // 6. Normalize HTTP method
        String normalizedMethod = (method == null || method.isBlank())
                ? null
                : method.trim().toUpperCase(java.util.Locale.ROOT);

        Page<WebhookEvent> events;

        // 7. Apply all filter combinations

        if (normalizedMethod == null) {

            // No HTTP method filter

            if (from != null && to != null) {

                events = eventRepository
                        .findAllByEndpoint_IdAndReceivedAtGreaterThanEqualAndReceivedAtLessThanEqualOrderByReceivedAtDesc(
                                endpoint.getId(), from, to, pageable
                        );

            } else if (from != null) {

                events = eventRepository
                        .findAllByEndpoint_IdAndReceivedAtGreaterThanEqualOrderByReceivedAtDesc(
                                endpoint.getId(), from, pageable
                        );

            } else if (to != null) {

                events = eventRepository
                        .findAllByEndpoint_IdAndReceivedAtLessThanEqualOrderByReceivedAtDesc(
                                endpoint.getId(), to, pageable
                        );

            } else {

                events = eventRepository
                        .findAllByEndpoint_IdOrderByReceivedAtDesc(
                                endpoint.getId(), pageable
                        );
            }

        } else {

            // HTTP method filter is provided

            if (from != null && to != null) {

                events = eventRepository
                        .findAllByEndpoint_IdAndHttpMethodIgnoreCaseAndReceivedAtGreaterThanEqualAndReceivedAtLessThanEqualOrderByReceivedAtDesc(
                                endpoint.getId(),
                                normalizedMethod,
                                from,
                                to,
                                pageable
                        );

            } else if (from != null) {

                events = eventRepository
                        .findAllByEndpoint_IdAndHttpMethodIgnoreCaseAndReceivedAtGreaterThanEqualOrderByReceivedAtDesc(
                                endpoint.getId(),
                                normalizedMethod,
                                from,
                                pageable
                        );

            } else if (to != null) {

                events = eventRepository
                        .findAllByEndpoint_IdAndHttpMethodIgnoreCaseAndReceivedAtLessThanEqualOrderByReceivedAtDesc(
                                endpoint.getId(),
                                normalizedMethod,
                                to,
                                pageable
                        );

            } else {

                events = eventRepository
                        .findAllByEndpoint_IdAndHttpMethodIgnoreCaseOrderByReceivedAtDesc(
                                endpoint.getId(),
                                normalizedMethod,
                                pageable
                        );
            }
        }

        // 8. Convert entities into DTOs
        return events.map(this::mapToResponse);
    }


    // Retrieve one event belonging to the authenticated user
    @Transactional(readOnly = true)
    public WebhookEventResponse getEventById(UUID eventId) {

        UUID userId = getAuthenticatedUserId();

        WebhookEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Webhook event not found"
                ));

        UUID eventOwnerId = event.getEndpoint()
                .getUser()
                .getId();

        if (!eventOwnerId.equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Webhook event not found"
            );
        }

        return mapToResponse(event);
    }

    // Identify the user through the authenticated API key
    private UUID getAuthenticatedUserId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UUID apiKeyId)) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid API key"
                ));

        if (apiKey.getRevokedAt() != null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid API key"
            );
        }

        return apiKey.getUser().getId();
    }

    // Convert entity into response DTO
    private WebhookEventResponse mapToResponse(WebhookEvent event) {

        String redactedJson = jsonRedactionService.redactJson(
                event.getParsedJson()
        );

        return new WebhookEventResponse(
                event.getId(),
                event.getEndpoint().getId(),
                event.getHttpMethod(),
                event.getRequestHeaders(),
                event.getQueryParameters(),
                event.getContentType(),
                redactedJson,
                event.getReceivedAt()
        );
    }

    @Transactional(readOnly = true)
    public WebhookEventStatsResponse getEventStatistics(UUID endpointId) {

        // 1. Authenticate the developer
        UUID userId = getAuthenticatedUserId();

        // 2. Verify endpoint ownership
        WebhookEndpoint endpoint = endpointRepository
                .findByIdAndUser_Id(endpointId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Webhook endpoint not found"
                ));

        // 3. Count all events for this endpoint
        long totalEvents = eventRepository.countByEndpoint_Id(
                endpoint.getId()
        );

        // 4. Retrieve event counts grouped by HTTP method
        List<Object[]> results = eventRepository.countEventsByHttpMethod(
                endpoint.getId()
        );

        // 5. Convert database results into a Map
        Map<String, Long> eventsByMethod = new LinkedHashMap<>();

        for (Object[] result : results) {

            String httpMethod = (String) result[0];

            Long eventCount = ((Number) result[1]).longValue();

            eventsByMethod.put(httpMethod, eventCount);
        }

        // 6. Return statistics response
        return new WebhookEventStatsResponse(
                totalEvents,
                eventsByMethod
        );
    }


}
