package com.transitfeed.service;

import com.transitfeed.service.BusArrivalParser.BusArrivalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Fetches the BusArrivalv2 payload from the LTA DataMall API for bus stop 83139.
 */
@Service
public class LtaArrivalService {

    private static final Logger log = LoggerFactory.getLogger(LtaArrivalService.class);
    private static final String BUS_STOP_CODE = "83139";

    private final RestClient ltaRestClient;
    private final String busStopCode;

    public LtaArrivalService(RestClient ltaRestClient,
                             @Value("${lta.api.bus-stop-code:83139}") String busStopCode) {
        this.ltaRestClient = ltaRestClient;
        this.busStopCode = busStopCode;
    }

    public BusArrivalResponse fetch() {
        try {
            BusArrivalResponse payload = ltaRestClient.get()
                    .uri(uri -> uri.path("/BusArrivalv2")
                            .queryParam("BusStopCode", busStopCode)
                            .build())
                    .retrieve()
                    .body(BusArrivalResponse.class);
            if (payload == null) {
                throw new IllegalStateException("LTA API returned an empty payload");
            }
            return payload;
        } catch (RestClientException e) {
            log.error("LTA DataMall request failed: {}", e.getMessage());
            throw new LtaApiException("Failed to reach the LTA DataMall API", e);
        }
    }

    public String getBusStopCode() {
        return busStopCode;
    }

    public static class LtaApiException extends RuntimeException {
        public LtaApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
