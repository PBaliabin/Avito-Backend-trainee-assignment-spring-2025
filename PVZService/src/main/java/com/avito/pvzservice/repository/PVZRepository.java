package com.avito.pvzservice.repository;

import com.avito.pvzservice.domain.PVZ;
import com.avito.pvzservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PVZRepository extends JpaRepository<PVZ, UUID> {
}
