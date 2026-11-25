package com.hooklens.hooklens.security;


import io.github.bucket4j.ConsumptionProbe;
import java.util.concurrent.TimeUnit;
import com.hooklens.hooklens.entity.ApiKey;
import com.hooklens.hooklens.repository.ApiKeyRepository;
import com.hooklens.hooklens.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Pattern API_KEY_PATTERN =
            Pattern.compile("^hl_live_([0-9a-f]{12})_([A-Za-z0-9_-]{43})$");

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitService rateLimitService;

    public ApiKeyAuthenticationFilter(
            ApiKeyRepository apiKeyRepository,
            PasswordEncoder passwordEncoder,
            RateLimitService rateLimitService
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.passwordEncoder = passwordEncoder;
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getServletPath()
                .startsWith("/api/v1/developer/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String apiKeyValue = request.getHeader("X-API-Key");

        if (apiKeyValue == null || apiKeyValue.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing or invalid API key"
            );
            return;
        }

        Matcher matcher = API_KEY_PATTERN.matcher(apiKeyValue);

        if (!matcher.matches()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing or invalid API key"
            );
            return;
        }

        String prefix = matcher.group(1);

        ApiKey apiKey = apiKeyRepository
                .findByKeyPrefix(prefix)
                .orElse(null);

        if (apiKey == null || apiKey.getRevokedAt() != null) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing or invalid API key"
            );
            return;
        }

        if (!passwordEncoder.matches(
                apiKeyValue,
                apiKey.getKeyHash()
        )) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing or invalid API key"
            );
            return;
        }

        apiKey.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(apiKey);

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        apiKey.getId(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_API_CLIENT")
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        // Check API rate limit

        ConsumptionProbe probe = rateLimitService.consume(apiKey.getId());

        long remaining = probe.getRemainingTokens();

        response.setHeader("X-Rate-Limit-Limit", "100");
        response.setHeader(
                "X-Rate-Limit-Remaining",
                String.valueOf(remaining)
        );

        if (!probe.isConsumed()) {

            long retryAfterSeconds = Math.max(
                    1,
                    TimeUnit.NANOSECONDS.toSeconds(
                            probe.getNanosToWaitForRefill()
                    ) + 1
            );

            response.setStatus(
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );

            response.setHeader(
                    "Retry-After",
                    String.valueOf(retryAfterSeconds)
            );

            response.setContentType("application/json");

            response.getWriter().write(
                    "{\"message\":\"Rate limit exceeded. Try again later.\"}"
            );

            return;
        }

        filterChain.doFilter(request, response);
    }
}
