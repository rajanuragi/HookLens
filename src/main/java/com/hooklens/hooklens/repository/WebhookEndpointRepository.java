
package com.hooklens.hooklens.repository;

import com.hooklens.hooklens.entity.WebhookEndpoint;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WebhookEndpointRepository
        extends JpaRepository<WebhookEndpoint, UUID> {

    List<WebhookEndpoint> findAllByUser_IdOrderByCreatedAtDesc(
            UUID userId
    );

    Optional<WebhookEndpoint> findByIdAndUser_Id(
            UUID endpointId,
            UUID userId
    );

    Optional<WebhookEndpoint> findByTokenPrefix(
            String tokenPrefix
    );
}
