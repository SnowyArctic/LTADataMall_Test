package com.transitfeed.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Parses the LTA DataMall BusArrivalv2 JSON payload and flattens it into
 * incoming arrivals (up to three per service: NextBus..NextBus3).
 */
@Service
public class BusArrivalParser {

    private static final String BUS_STOP_CODE = "83139";

    public record Arrival(String serviceNo, String estimatedArrival, String load) {
    }

    public record Result(String busStopCode, List<Arrival> arrivals) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BusArrivalResponse {
        @JsonProperty("Services")
        public List<Service> services;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Service {
            @JsonProperty("ServiceNo")
            public String serviceNo;
            @JsonProperty("NextBus")
            public NextBus nextBus;
            @JsonProperty("NextBus2")
            public NextBus nextBus2;
            @JsonProperty("NextBus3")
            public NextBus nextBus3;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class NextBus {
            @JsonProperty("EstimatedArrival")
            public String estimatedArrival;
            @JsonProperty("Load")
            public String load;
        }
    }

    public Result parse(BusArrivalResponse payload) {
        List<Arrival> arrivals = new java.util.ArrayList<>();
        if (payload == null || payload.services == null) {
            return new Result(BUS_STOP_CODE, List.of());
        }

        for (BusArrivalResponse.Service service : payload.services) {
            if (service == null || service.serviceNo == null || service.serviceNo.isBlank()) {
                continue;
            }
            collect(service.nextBus, service.serviceNo, arrivals);
            collect(service.nextBus2, service.serviceNo, arrivals);
            collect(service.nextBus3, service.serviceNo, arrivals);
        }

        arrivals.sort(java.util.Comparator.comparing(
                Arrival::estimatedArrival,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())));

        return new Result(BUS_STOP_CODE, List.copyOf(arrivals));
    }

    private void collect(BusArrivalResponse.NextBus bus, String serviceNo, List<Arrival> out) {
        if (bus == null || bus.estimatedArrival == null || bus.estimatedArrival.isBlank()) {
            return;
        }
        out.add(new Arrival(serviceNo, bus.estimatedArrival.trim(), bus.load));
    }
}
