package com.avito.pvzservice.model.response.auth;

import com.avito.pvzservice.domain.User;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterResponse {
  private UUID id;
  private String email;
  private User.Role role;
}
