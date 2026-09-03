package com.transitfeed.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.springframework.http.HttpHeaders.ACCEPT;

/**
 * REST client for the LTA DataMall API with the AccountKey header.
 */
@Configuration
public class LtaApiClientConfig {

    @Bean
    public RestClient ltaRestClient(
            @Value("${lta.api.base-url}") String baseUrl,
            @Value("${lta.api.account-key}") String accountKey) {

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("AccountKey", accountKey)
                .defaultHeader(ACCEPT, "application/json")
                .build();
    }
}
