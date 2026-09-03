package com.transitfeed.web;

import com.transitfeed.service.ArrivalPersistenceService;
import com.transitfeed.service.BusArrivalParser;
import com.transitfeed.service.BusArrivalParser.Result;
import com.transitfeed.service.LtaArrivalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GET /api/transit-feed — server-side fetch of the LTA DataMall Bus Arrival
 * API (v3) for bus stop 83139, returning only the incoming arrival timestamps.
 */
@RestController
@RequestMapping("/api")
public class TransitFeedController {

    private final LtaArrivalService ltaArrivalService;
    private final BusArrivalParser parser;
    private final ArrivalPersistenceService persistenceService;

    public TransitFeedController(LtaArrivalService ltaArrivalService,
                                 BusArrivalParser parser,
                                 ArrivalPersistenceService persistenceService) {
        this.ltaArrivalService = ltaArrivalService;
        this.parser = parser;
        this.persistenceService = persistenceService;
    }

    @GetMapping("/transit-feed")
    public ResponseEntity<Map<String, Object>> transitFeed() {
        try {
            BusArrivalParser.BusArrivalResponse payload = ltaArrivalService.fetch();
            Result result = parser.parse(payload);
            persistenceService.persist(result);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("busStopCode", result.busStopCode());
            body.put("arrivals", result.arrivals());
            return ResponseEntity.ok(body);
        } catch (LtaArrivalService.LtaApiException e) {
            return error(HttpStatus.valueOf(e.getStatus().value()), e.getMessage());
        } catch (Exception e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error fetching the transit feed");
        }
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ok");
        body.put("busStopCode", ltaArrivalService.getBusStopCode());
        return body;
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}
