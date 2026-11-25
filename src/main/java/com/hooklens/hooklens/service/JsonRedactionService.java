
package com.hooklens.hooklens.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Map;

@Service
public class JsonRedactionService {

    private static final String REDACTED = "[REDACTED]";

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password",
            "passwd",
            "token",
            "access_token",
            "refresh_token",
            "apikey",
            "api_key",
            "client_secret",
            "webhook_secret",
            "secret",
            "authorization",
            "signature",
            "sig",
            "cookie",
            "set_cookie",
            "private_key",
            "credentials"
    );

    private final ObjectMapper objectMapper;

    public JsonRedactionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String redactJson(String json) {

        if (json == null || json.isBlank()) {
            return json;
        }

        try {
            JsonNode root = objectMapper.readTree(json);

            redactNode(root);

            return objectMapper.writeValueAsString(root);

        } catch (Exception exception) {
            // Do not expose an unparseable payload in the API response.
            return "[JSON REDACTION UNAVAILABLE]";
        }
    }



    private void redactNode(JsonNode node) {

        if (node == null) {
            return;
        }

        if (node.isObject()) {

            ObjectNode objectNode = (ObjectNode) node;

            for (String key : objectNode.propertyNames()) {

                String normalizedKey = key
                        .toLowerCase(Locale.ROOT)
                        .replace("-", "")
                        .replace("_", "");

                if (isSensitiveKey(normalizedKey)) {

                    objectNode.put(key, REDACTED);

                } else {

                    redactNode(objectNode.get(key));
                }
            }

        } else if (node.isArray()) {

            for (JsonNode child : node) {
                redactNode(child);
            }
        }
    }




    private boolean isSensitiveKey(String key) {

        for (String sensitiveKey : SENSITIVE_KEYS) {

            String normalizedSensitiveKey = sensitiveKey
                    .toLowerCase(Locale.ROOT)
                    .replace("-", "")
                    .replace("_", "");

            if (key.equals(normalizedSensitiveKey)
                    || key.endsWith(normalizedSensitiveKey)) {
                return true;
            }
        }

        return false;
    }

}
