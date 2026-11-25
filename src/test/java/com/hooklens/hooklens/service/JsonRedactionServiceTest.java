
package com.hooklens.hooklens.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

class JsonRedactionServiceTest {

    private JsonRedactionService redactionService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        redactionService = new JsonRedactionService(objectMapper);
    }

    @Test
    void shouldRedactPassword() {

        String json = """
                {
                    "username": "john",
                    "password": "secret123"
                }
                """;

        String result = redactionService.redactJson(json);

        assertTrue(result.contains("[REDACTED]"));
        assertFalse(result.contains("secret123"));
        assertTrue(result.contains("john"));
    }

    @Test
    void shouldRedactNestedSensitiveFields() {

        String json = """
                {
                    "user": {
                        "email": "test@example.com",
                        "client_secret": "abc123"
                    }
                }
                """;

        String result = redactionService.redactJson(json);

        assertTrue(result.contains("[REDACTED]"));
        assertFalse(result.contains("abc123"));
        assertTrue(result.contains("test@example.com"));
    }

    @Test
    void shouldRedactSensitiveFieldsInsideArrays() {

        String json = """
                {
                    "items": [
                        {
                            "api_key": "key123"
                        }
                    ]
                }
                """;

        String result = redactionService.redactJson(json);

        assertTrue(result.contains("[REDACTED]"));
        assertFalse(result.contains("key123"));
    }

    @Test
    void shouldRedactCaseInsensitiveKeys() {

        String json = """
                {
                    "ACCESS_TOKEN": "token123"
                }
                """;

        String result = redactionService.redactJson(json);

        assertTrue(result.contains("[REDACTED]"));
        assertFalse(result.contains("token123"));
    }

    @Test
    void shouldRedactSuffixMatchingKeys() {

        String json = """
                {
                    "user_password": "pass123",
                    "customer_access_token": "token123",
                    "x-api-key": "key123"
                }
                """;

        String result = redactionService.redactJson(json);

        assertFalse(result.contains("pass123"));
        assertFalse(result.contains("token123"));
        assertFalse(result.contains("key123"));
        assertTrue(result.contains("[REDACTED]"));
    }

    @Test
    void shouldPreserveNormalFields() {

        String json = """
                {
                    "name": "HookLens",
                    "email": "test@example.com"
                }
                """;

        String result = redactionService.redactJson(json);

        assertTrue(result.contains("HookLens"));
        assertTrue(result.contains("test@example.com"));
    }

    @Test
    void shouldReturnNullForNullInput() {

        String result = redactionService.redactJson(null);

        assertNull(result);
    }

    @Test
    void shouldReturnUnavailableForInvalidJson() {

        String result = redactionService.redactJson("{invalid-json");

        assertEquals("[JSON REDACTION UNAVAILABLE]", result);
    }
}

