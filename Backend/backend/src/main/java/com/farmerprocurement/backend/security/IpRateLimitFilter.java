package com.farmerprocurement.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IpRateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000;

    private static final int LOGIN_LIMIT = 5;
    private static final int REGISTER_LIMIT = 3;

    private final ConcurrentHashMap<String, Deque<Long>> requestRecords =
            new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String endpoint = request.getRequestURI();

        if (!HttpMethod.POST.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        int limit;

        if ("/api/auth/login".equals(endpoint)) {
            limit = LOGIN_LIMIT;
        } else if ("/api/auth/register".equals(endpoint)) {
            limit = REGISTER_LIMIT;
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();

        String key = clientIp + ":" + endpoint;

        long currentTime = Instant.now().toEpochMilli();

        Deque<Long> timestamps = requestRecords.computeIfAbsent(
                key,
                ignored -> new ArrayDeque<>()
        );

        synchronized (timestamps) {

            while (!timestamps.isEmpty()
                    && currentTime - timestamps.peekFirst() >= WINDOW_MILLIS) {

                timestamps.pollFirst();
            }

            if (timestamps.size() >= limit) {

                response.setStatus(429);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                response.getWriter().write(
                        "{\"status\":429,\"message\":\"Too many requests. Please try again after one minute.\"}"
                );

                return;
            }

            timestamps.addLast(currentTime);
        }

        filterChain.doFilter(request, response);
    }
}