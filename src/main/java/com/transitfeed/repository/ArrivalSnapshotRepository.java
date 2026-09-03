package com.transitfeed.repository;

import com.transitfeed.model.ArrivalSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface ArrivalSnapshotRepository extends JpaRepository<ArrivalSnapshot, Long> {

    /**
     * Keeps the table bounded: drops snapshots older than the retention window
     * (the LTA API only reports arrivals for the next ~30 minutes).
     */
    @Modifying
    @Query("DELETE FROM ArrivalSnapshot a WHERE a.fetchedAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);
}
