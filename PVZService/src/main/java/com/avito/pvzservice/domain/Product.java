package com.avito.pvzservice.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {
  @Id
//  @GeneratedValue(generator="system-uuid")
  @Column(name = "uuid", unique = true)
  private UUID uuid;

  @Column(name = "date_time", nullable = false)
  private Instant dateTime;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private Type type;

  @PrePersist
  public void generateIfNotSet() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID();
    }
    if (this.dateTime == null) {
      this.dateTime = Instant.now();
    }
  }

  @Getter
  public enum Type {
    ELECTRONICS("электроника"),
    CLOTHING("одежда"),
    SHOES("обувь");

    private final String type;

    Type(String type) {
      this.type = type;
    }

    @JsonValue
    public String getType() {
      return this.type;
    }

    @JsonCreator
    public static Type fromString(String value) {
      for (Type type : values()) {
        if (type.type.equalsIgnoreCase(value)) {
          return type;
        }
      }
      throw new IllegalArgumentException("Unknown type: " + value);
    }

    @Override
    public String toString() {
      return type;
    }
  }
}
