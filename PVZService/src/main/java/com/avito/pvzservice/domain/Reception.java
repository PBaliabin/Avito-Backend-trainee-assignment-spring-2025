package com.avito.pvzservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "reception")
public class Reception {
  @Id
  @Column(name = "uuid", unique = true)
  private UUID uuid;

  @Column(name = "date_time", nullable = false)
  private Instant dateTime;

  @Column(name = "pvz_uuid", nullable = false)
  private UUID pvzUuid;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "reception_uuid")
  @OrderColumn(name = "product_order")
  private List<Product> products;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private Status status;

  @PrePersist
  public void generateIfNotSet() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID();
    }
    if (this.dateTime == null) {
      this.dateTime = Instant.now();
    }
  }

  public enum Status {
    in_progress,
    close
  }

  public void removeLastProduct() {
    if (products.isEmpty()) {
      return;
    }
    products.removeLast();
  }

  public void addProduct(Product product) {
    products.add(product);
  }
}
