
package com.hooklens.hooklens.service;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class RateLimitService {

    private final ConcurrentMap<UUID, Bucket> buckets =
            new ConcurrentHashMap<>();

    private Bucket createBucket() {

        Bandwidth limit = Bandwidth.classic(
                100,
                Refill.intervally(
                        100,
                        Duration.ofMinutes(1)
                )
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public ConsumptionProbe consume(UUID apiKeyId) {

        Bucket bucket = buckets.computeIfAbsent(
                apiKeyId,
                id -> createBucket()
        );

        return bucket.tryConsumeAndReturnRemaining(1);
    }
}
