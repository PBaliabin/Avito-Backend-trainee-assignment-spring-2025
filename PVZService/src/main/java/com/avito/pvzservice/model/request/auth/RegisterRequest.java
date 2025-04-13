package com.avito.pvzservice.model.request.auth;

import com.avito.pvzservice.domain.User;
import lombok.Data;

@Data
public class RegisterRequest {
  private String email;
  private String password;
  private User.Role role;
}
