package com.avito.pvzservice.repository;

import com.avito.pvzservice.domain.Product;
import com.avito.pvzservice.domain.Reception;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReceptionRepository extends JpaRepository<Reception, UUID> {
    @Query("SELECT r FROM Reception r WHERE r.dateTime BETWEEN :start AND :end")
    Page<Reception> findByDateRange(
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable
    );

    @Query("SELECT r FROM Reception r WHERE r.pvzUuid = :pvzId ORDER BY r.dateTime DESC LIMIT 1")
    Optional<Reception> findLatestByPvzId(@Param("pvzId") UUID pvzId);
}
