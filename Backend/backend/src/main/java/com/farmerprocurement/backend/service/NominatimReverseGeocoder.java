package com.farmerprocurement.backend.service;

import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NominatimReverseGeocoder implements ReverseGeocoder {

    private static final long MIN_REQUEST_INTERVAL_NANOS = 1_000_000_000L;
    private final RestClient restClient;
    private final String contact;
    private final AtomicLong nextRequestNanos = new AtomicLong();

    public NominatimReverseGeocoder(
            @Qualifier("nominatimRestClient") RestClient restClient,
            @Value("${app.nominatim.contact:}") String contact) {
        this.restClient = restClient;
        this.contact = contact;
    }

    @Override
    public String reverse(double latitude, double longitude) {
        if (contact == null || contact.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Location lookup is not configured"
            );
        }
        reserveRequestSlot();
        try {
            JsonNode result = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reverse")
                            .queryParam("format", "jsonv2")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("zoom", 10)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
            JsonNode displayName = result == null ? null : result.get("display_name");
            if (displayName == null || !displayName.isTextual()
                    || displayName.asText().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Location provider returned no address"
                );
            }
            String label = displayName.asText().strip();
            return label.length() <= 300 ? label : label.substring(0, 300);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Location provider is temporarily unavailable"
            );
        }
    }

    private void reserveRequestSlot() {
        long now = System.nanoTime();
        while (true) {
            long nextAllowed = nextRequestNanos.get();
            if (now < nextAllowed) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Please wait before requesting another location lookup"
                );
            }
            if (nextRequestNanos.compareAndSet(
                    nextAllowed, now + MIN_REQUEST_INTERVAL_NANOS)) {
                return;
            }
            now = System.nanoTime();
        }
    }
}
