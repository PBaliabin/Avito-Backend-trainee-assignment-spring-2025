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
@Table(name = "pvz")
public class PVZ {
  @Id
  @Column(name = "uuid", unique = true)
  private UUID uuid;

  @Column(name = "registration_date", nullable = false)
  private Instant registrationDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "city", nullable = false)
  private City city;

  @PrePersist
  public void generateIfNotSet() {
    if (this.uuid == null) {
      this.uuid = UUID.randomUUID();
    }
    if (this.registrationDate == null) {
      this.registrationDate = Instant.now();
    }
  }

  @Getter
  public enum City {
    MOSCOW("Москва"),
    SAINT_PETERSBURG("Санкт-Петербург"),
    KAZAN("Казань");

    private final String cityName;

    City(String cityName) {
      this.cityName = cityName;
    }

    @JsonValue
    public String getCityName() {
      return cityName;
    }

    @JsonCreator
    public static City fromString(String value) {
      for (City city : values()) {
        if (city.cityName.equalsIgnoreCase(value)) {
          return city;
        }
      }
      throw new IllegalArgumentException("Unknown city: " + value);
    }

    @Override
    public String toString() {
      return cityName;
    }
  }
}