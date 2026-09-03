package com.transitfeed.service;

import com.transitfeed.service.BusArrivalParser.Arrival;
import com.transitfeed.service.BusArrivalParser.BusArrivalResponse;
import com.transitfeed.service.BusArrivalParser.Result;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BusArrivalParserTest {

    private final BusArrivalParser parser = new BusArrivalParser();

    private BusArrivalResponse.NextBus nextBus(String eta, String load) {
        BusArrivalResponse.NextBus bus = new BusArrivalResponse.NextBus();
        bus.estimatedArrival = eta;
        bus.load = load;
        return bus;
    }

    @Test
    void flattensAllNextBusSlotsAndSortsByArrival() {
        BusArrivalResponse payload = new BusArrivalResponse();

        BusArrivalResponse.Service s964 = new BusArrivalResponse.Service();
        s964.serviceNo = "964";
        s964.nextBus = nextBus("2026-08-31T10:05:00+08:00", "SEA");
        s964.nextBus2 = nextBus("2026-08-31T10:12:00+08:00", "SDA");
        s964.nextBus3 = new BusArrivalResponse.NextBus(); // empty slot

        BusArrivalResponse.Service s117 = new BusArrivalResponse.Service();
        s117.serviceNo = "117";
        s117.nextBus = nextBus("2026-08-31T10:02:00+08:00", "LSD");

        payload.services = List.of(s964, s117);

        Result result = parser.parse(payload);

        assertThat(result.busStopCode()).isEqualTo("83139");
        assertThat(result.arrivals()).extracting(Arrival::serviceNo)
                .containsExactly("117", "964", "964");
        assertThat(result.arrivals()).extracting(Arrival::estimatedArrival)
                .containsExactly(
                        "2026-08-31T10:02:00+08:00",
                        "2026-08-31T10:05:00+08:00",
                        "2026-08-31T10:12:00+08:00");
    }

    @Test
    void skipsEmptySlotsAndNullServices() {
        BusArrivalResponse payload = new BusArrivalResponse();

        BusArrivalResponse.Service s = new BusArrivalResponse.Service();
        s.serviceNo = "964";
        s.nextBus = new BusArrivalResponse.NextBus(); // no ETA
        s.nextBus2 = nextBus("2026-08-31T10:20:00+08:00", "SEA");
        payload.services = java.util.Arrays.asList(s, null);

        Result result = parser.parse(payload);

        assertThat(result.arrivals()).hasSize(1);
        assertThat(result.arrivals().get(0).serviceNo()).isEqualTo("964");
        assertThat(result.arrivals().get(0).estimatedArrival()).isEqualTo("2026-08-31T10:20:00+08:00");
    }

    @Test
    void emptyPayloadYieldsEmptyList() {
        assertThat(parser.parse(null).arrivals()).isEmpty();
        assertThat(parser.parse(new BusArrivalResponse()).arrivals()).isEmpty();
    }

    @Test
    void mapsLtaPascalCaseJsonThroughJackson() throws Exception {
        String json = """
                {"Services":[
                  {"ServiceNo":"964",
                   "NextBus":{"EstimatedArrival":"2026-08-31T10:05:00+08:00","Load":"SEA"},
                   "NextBus2":{"EstimatedArrival":"2026-08-31T10:12:00+08:00","Load":"SDA"},
                   "NextBus3":{}},
                  {"ServiceNo":"117",
                   "NextBus":{"EstimatedArrival":"2026-08-31T10:02:00+08:00","Load":"LSD"},
                   "NextBus2":{},"NextBus3":{}}
                ]}
                """;

        BusArrivalResponse payload = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(json, BusArrivalResponse.class);
        Result result = parser.parse(payload);

        assertThat(result.busStopCode()).isEqualTo("83139");
        assertThat(result.arrivals()).hasSize(3);
        assertThat(result.arrivals()).extracting(Arrival::serviceNo)
                .containsExactly("117", "964", "964");
        assertThat(result.arrivals()).extracting(Arrival::estimatedArrival)
                .containsExactly(
                        "2026-08-31T10:02:00+08:00",
                        "2026-08-31T10:05:00+08:00",
                        "2026-08-31T10:12:00+08:00");
    }
}
