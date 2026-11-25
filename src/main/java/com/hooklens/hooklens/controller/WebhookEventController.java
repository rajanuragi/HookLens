
package com.hooklens.hooklens.controller;

import com.hooklens.hooklens.dto.response.WebhookEventStatsResponse;
import java.time.LocalDateTime;
import com.hooklens.hooklens.dto.WebhookEventResponse;
import com.hooklens.hooklens.service.WebhookEventService;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/developer")
public class WebhookEventController {

    private final WebhookEventService eventService;

    public WebhookEventController(WebhookEventService eventService) {
        this.eventService = eventService;
    }

    // Get paginated events for a specific endpoint


    @GetMapping("/endpoints/{endpointId}/events")
    public Page<WebhookEventResponse> getEventsForEndpoint(
            @PathVariable UUID endpointId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String method,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime from,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(
                    iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME
            )
            LocalDateTime to
    ) {
        return eventService.getPaginatedEvents(
                endpointId,
                page,
                size,
                method,
                from,
                to
        );
    }



    // Get a specific event by ID
    @GetMapping("/events/{eventId}")
    public WebhookEventResponse getEventById(
            @PathVariable UUID eventId
    ) {
        return eventService.getEventById(eventId);
    }

    @GetMapping("/endpoints/{endpointId}/stats")
    public WebhookEventStatsResponse getEventStatistics(
            @PathVariable UUID endpointId
    ) {
        return eventService.getEventStatistics(endpointId);
    }
}
