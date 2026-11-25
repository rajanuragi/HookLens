
package com.hooklens.hooklens.controller;

import com.hooklens.hooklens.service.WebhookIngestionService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
public class WebhookIngestionController {

    private static final int MAX_BODY_SIZE = 1024 * 1024;

    private final WebhookIngestionService ingestionService;

    public WebhookIngestionController(
            WebhookIngestionService ingestionService
    ) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/e/{token}")
    public ResponseEntity<Map<String, Object>> receiveWebhook(
            @PathVariable String token,
            HttpServletRequest request
    ) {

        try {

            // 1. Check Content-Length before reading the body

            if (request.getContentLengthLong() > MAX_BODY_SIZE) {

                return ResponseEntity
                        .status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(Map.of(
                                "status", "error",
                                "message", "Request body exceeds 1 MB limit"
                        ));
            }

            // 2. Read request body with a strict size limit

            byte[] body = readRequestBody(request);

            // 3. Pass request to ingestion service

            UUID eventId = ingestionService.ingestWebhook(
                    token,
                    request,
                    body
            );

            // 4. Return success only after service transaction commits

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Map.of(
                            "status", "success",
                            "message", "Webhook received successfully",
                            "eventId", eventId
                    ));

        } catch (PayloadTooLargeException exception) {

            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of(
                            "status", "error",
                            "message", "Request body exceeds 1 MB limit"
                    ));

        } catch (IllegalArgumentException exception) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", "error",
                            "message", "Invalid webhook token"
                    ));

        } catch (IOException exception) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "status", "error",
                            "message", "Unable to read request body"
                    ));

        } catch (RuntimeException exception) {

            // Database or unexpected processing failure.
            // Never return success when persistence fails.

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Webhook processing failed"
                    ));
        }
    }

    // Read at most 1 MB + 1 byte to detect oversized requests

    private byte[] readRequestBody(
            HttpServletRequest request
    ) throws IOException, PayloadTooLargeException {

        byte[] body = request.getInputStream()
                .readNBytes(MAX_BODY_SIZE + 1);

        if (body.length > MAX_BODY_SIZE) {
            throw new PayloadTooLargeException();
        }

        return body;
    }

    // Internal exception for oversized requests

    private static class PayloadTooLargeException
            extends Exception {
    }
}

