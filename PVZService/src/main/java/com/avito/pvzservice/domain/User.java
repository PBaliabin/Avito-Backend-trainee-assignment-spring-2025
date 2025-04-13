package com.avito.pvzservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_pvz_service")
public class User {
  @Id
  @GeneratedValue(generator="system-uuid")
  @Column(name = "uuid", unique = true)
  private UUID uuid;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;

  @Getter
  public enum Role {
    employee,
    moderator
  }
}
