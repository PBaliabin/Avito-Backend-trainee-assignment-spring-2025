package com.avito.pvzservice.repository;

import com.avito.pvzservice.domain.PVZ;
import com.avito.pvzservice.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
}
