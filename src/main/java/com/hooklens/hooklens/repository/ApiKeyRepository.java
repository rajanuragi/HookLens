
package com.hooklens.hooklens.repository;

import com.hooklens.hooklens.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository
        extends JpaRepository<ApiKey, UUID> {

    List<ApiKey> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);

    Optional<ApiKey> findByKeyPrefix(String keyPrefix);

}
