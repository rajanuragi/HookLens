
package com.hooklens.hooklens.repository;

import com.hooklens.hooklens.entity.WebhookEvent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

public interface WebhookEventRepository
        extends JpaRepository<WebhookEvent, UUID> {

    List<WebhookEvent> findAllByEndpoint_IdOrderByReceivedAtDesc(
            UUID endpointId
    );
    Page<WebhookEvent> findAllByEndpoint_IdOrderByReceivedAtDesc(
            UUID endpointId,
            Pageable pageable
    );
    Page<WebhookEvent> findAllByEndpoint_IdAndHttpMethodIgnoreCaseOrderByReceivedAtDesc(
            UUID endpointId,
            String httpMethod,
            Pageable pageable
    );
    Page<WebhookEvent>
    findAllByEndpoint_IdAndReceivedAtGreaterThanEqualAndReceivedAtLessThanEqualOrderByReceivedAtDesc(
            UUID endpointId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    Page<WebhookEvent>
    findAllByEndpoint_IdAndReceivedAtGreaterThanEqualOrderByReceivedAtDesc(
            UUID endpointId,
            LocalDateTime from,
            Pageable pageable
    );

    Page<WebhookEvent>
    findAllByEndpoint_IdAndReceivedAtLessThanEqualOrderByReceivedAtDesc(
            UUID endpointId,
            LocalDateTime to,
            Pageable pageable
    );

    Page<WebhookEvent>
    findAllByEndpoint_IdAndHttpMethodIgnoreCaseAndReceivedAtGreaterThanEqualAndReceivedAtLessThanEqualOrderByReceivedAtDesc(
            UUID endpointId,
            String httpMethod,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    Page<WebhookEvent>
    findAllByEndpoint_IdAndHttpMethodIgnoreCaseAndReceivedAtGreaterThanEqualOrderByReceivedAtDesc(
            UUID endpointId,
            String httpMethod,
            LocalDateTime from,
            Pageable pageable
    );

    Page<WebhookEvent>
    findAllByEndpoint_IdAndHttpMethodIgnoreCaseAndReceivedAtLessThanEqualOrderByReceivedAtDesc(
            UUID endpointId,
            String httpMethod,
            LocalDateTime to,
            Pageable pageable
    );

    long countByEndpoint_Id(UUID endpointId);
    @Query("""
    SELECT e.httpMethod AS httpMethod, COUNT(e) AS eventCount
    FROM WebhookEvent e
    WHERE e.endpoint.id = :endpointId
    GROUP BY e.httpMethod
    """)
    List<Object[]> countEventsByHttpMethod(
            @Param("endpointId") UUID endpointId
    );
}
