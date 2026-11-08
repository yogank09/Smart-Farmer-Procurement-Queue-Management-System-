package com.farmerprocurement.backend.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class NominatimConfiguration {

    @Bean("nominatimRestClient")
    RestClient nominatimRestClient(
            @Value("${app.nominatim.contact:}") String contact) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        return RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .requestFactory(requestFactory)
                .defaultHeader(
                        "User-Agent",
                        "KisanMitra/1.0 (explicit location lookup; contact: "
                                + contact + ")")
                .build();
    }
}
