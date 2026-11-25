
package com.hooklens.hooklens.dto.response ;
import java.util.Map;
import java.util.LinkedHashMap;
import com.hooklens.hooklens.dto.response.WebhookEventStatsResponse;
public record WebhookEventStatsResponse(
        long totalEvents,
        Map<String, Long> eventsByMethod
) {
}
