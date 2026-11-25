
package com.hooklens.hooklens.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "webhook_events",
        indexes = {
                @Index(
                        name = "idx_event_endpoint_id",
                        columnList = "endpoint_id"
                ),
                @Index(
                        name = "idx_event_received_at",
                        columnList = "received_at"
                )
        }
)
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    private WebhookEndpoint endpoint;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders;

    @Column(name = "query_parameters", columnDefinition = "TEXT")
    private String queryParameters;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Column(name = "raw_body", columnDefinition = "BYTEA")
    private byte[] rawBody;

    @Column(name = "parsed_json", columnDefinition = "TEXT")
    private String parsedJson;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    public WebhookEvent() {}

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public WebhookEndpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(WebhookEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getRawBody() {
        return rawBody;
    }

    public void setRawBody(byte[] rawBody) {
        this.rawBody = rawBody;
    }

    public String getParsedJson() {
        return parsedJson;
    }

    public void setParsedJson(String parsedJson) {
        this.parsedJson = parsedJson;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }
}
