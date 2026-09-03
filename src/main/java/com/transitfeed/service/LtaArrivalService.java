package com.transitfeed.service;

import com.transitfeed.service.BusArrivalParser.BusArrivalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Fetches the Bus Arrival payload (v3 endpoint) from the LTA DataMall API
 * for bus stop 83139.
 */
@Service
public class LtaArrivalService {

    private static final Logger log = LoggerFactory.getLogger(LtaArrivalService.class);
    private static final String BUS_STOP_CODE = "83139";

    private final RestClient ltaRestClient;
    private final String accountKey;
    private final String busStopCode;
    private final String baseUrl;

    public LtaArrivalService(RestClient ltaRestClient,
                             @Value("${lta.api.base-url}") String baseUrl,
                             @Value("${lta.api.account-key}") String accountKey,
                             @Value("${lta.api.bus-stop-code:83139}") String busStopCode) {
        this.ltaRestClient = ltaRestClient;
        this.baseUrl = baseUrl;
        this.accountKey = accountKey;
        this.busStopCode = busStopCode;
    }

    public BusArrivalResponse fetch() {
        if (accountKey == null || accountKey.isBlank()) {
            throw new LtaApiException(HttpStatusCode.valueOf(500),
                    "Missing LTA_ACCOUNT_KEY - set it in the environment to call the LTA DataMall API");
        }
        try {
            BusArrivalResponse payload = ltaRestClient.get()
                    .uri(uri -> uri.path("/BusArrival")
                            .queryParam("BusStopCode", busStopCode)
                            .build())
                    .retrieve()
                    .body(BusArrivalResponse.class);
            if (payload == null) {
                throw new IllegalStateException("LTA API returned an empty payload");
            }
            return payload;
        } catch (RestClientResponseException e) {
            HttpStatusCode status = e.getStatusCode();
            log.error("LTA DataMall request rejected with HTTP {}: {}",
                    status.value(), e.getResponseBodyAsString());
            if (status.value() == 401 || status.value() == 403 || status.value() == 404) {
                throw new LtaApiException(HttpStatusCode.valueOf(502),
                        "LTA DataMall rejected the request (HTTP " + status.value() + ") at " + baseUrl
                                + " - check that LTA_ACCOUNT_KEY is valid and the API URL is correct");
            }
            throw new LtaApiException(HttpStatusCode.valueOf(502),
                    "LTA DataMall returned HTTP " + status.value() + " at " + baseUrl, e);
        } catch (RestClientException e) {
            log.error("LTA DataMall request failed: {}", e.getMessage());
            throw new LtaApiException(HttpStatusCode.valueOf(502),
                    "Failed to reach the LTA DataMall API at " + baseUrl + " - " + rootCauseMessage(e), e);
        }
    }

    public String getBusStopCode() {
        return busStopCode;
    }

    private static String rootCauseMessage(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause.getClass().getSimpleName()
                + (cause.getMessage() != null ? ": " + cause.getMessage() : "");
    }

    public static class LtaApiException extends RuntimeException {
        private final HttpStatusCode status;

        public LtaApiException(HttpStatusCode status, String message) {
            super(message);
            this.status = status;
        }

        public LtaApiException(HttpStatusCode status, String message, Throwable cause) {
            super(message, cause);
            this.status = status;
        }

        public HttpStatusCode getStatus() {
            return status;
        }
    }
}