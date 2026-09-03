package com.transitfeed.service;

import com.transitfeed.model.ArrivalSnapshot;
import com.transitfeed.repository.ArrivalSnapshotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Persists each fetched arrival snapshot into PostgreSQL (or the dev fallback
 * database) and prunes rows older than the retention window.
 */
@Service
public class ArrivalPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(ArrivalPersistenceService.class);

    private final ArrivalSnapshotRepository repository;
    private final boolean persistenceEnabled;
    private final int retentionMinutes;

    public ArrivalPersistenceService(
            ArrivalSnapshotRepository repository,
            @Value("${transit.persistence.enabled:true}") boolean persistenceEnabled,
            @Value("${transit.persistence.retention-minutes:60}") int retentionMinutes) {
        this.repository = repository;
        this.persistenceEnabled = persistenceEnabled;
        this.retentionMinutes = retentionMinutes;
    }

    @Transactional
    public void persist(BusArrivalParser.Result result) {
        if (!persistenceEnabled) {
            return;
        }
        Instant now = Instant.now();
        List<ArrivalSnapshot> rows = result.arrivals().stream()
                .map(a -> new ArrivalSnapshot(
                        result.busStopCode(),
                        a.serviceNo(),
                        a.estimatedArrival(),
                        parseInstant(a.estimatedArrival()),
                        a.load(),
                        now))
                .toList();
        repository.saveAll(rows);

        int removed = repository.deleteOlderThan(now.minusSeconds(retentionMinutes * 60L));
        if (removed > 0) {
            log.debug("Pruned {} expired arrival snapshots", removed);
        }
    }

    private OffsetDateTime parseInstant(String iso) {
        if (iso == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(iso);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
