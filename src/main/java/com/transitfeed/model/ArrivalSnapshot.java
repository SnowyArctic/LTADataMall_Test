package com.transitfeed.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.OffsetDateTime;

/**
 * A persisted snapshot of incoming bus arrivals for a bus stop,
 * as returned by the LTA DataMall Bus Arrival API (v3).
 */
@Entity
@Table(name = "arrival_snapshots", indexes = {
        @Index(name = "idx_arrival_snapshots_stop_code", columnList = "busStopCode"),
        @Index(name = "idx_arrival_snapshots_fetched_at", columnList = "fetchedAt")
})
public class ArrivalSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bus_stop_code", nullable = false, length = 10)
    private String busStopCode;

    @Column(name = "service_no", nullable = false, length = 10)
    private String serviceNo;

    /** ISO-8601 estimated arrival timestamp as returned by the LTA API. */
    @Column(name = "estimated_arrival", length = 64)
    private String estimatedArrival;

    /** Parsed arrival instant (nullable when the API returns no timestamp). */
    @Column(name = "estimated_arrival_at")
    private OffsetDateTime estimatedArrivalAt;

    /** SEA / SDA / LSD bus load indicator from the LTA API. */
    @Column(name = "load", length = 8)
    private String load;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;

    protected ArrivalSnapshot() {
        // JPA
    }

    public ArrivalSnapshot(String busStopCode, String serviceNo, String estimatedArrival,
                           OffsetDateTime estimatedArrivalAt, String load, Instant fetchedAt) {
        this.busStopCode = busStopCode;
        this.serviceNo = serviceNo;
        this.estimatedArrival = estimatedArrival;
        this.estimatedArrivalAt = estimatedArrivalAt;
        this.load = load;
        this.fetchedAt = fetchedAt;
    }

    public Long getId() {
        return id;
    }

    public String getBusStopCode() {
        return busStopCode;
    }

    public String getServiceNo() {
        return serviceNo;
    }

    public String getEstimatedArrival() {
        return estimatedArrival;
    }

    public OffsetDateTime getEstimatedArrivalAt() {
        return estimatedArrivalAt;
    }

    public String getLoad() {
        return load;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }
}
