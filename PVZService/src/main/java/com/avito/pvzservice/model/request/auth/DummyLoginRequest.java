package com.avito.pvzservice.model.request.auth;

import com.avito.pvzservice.domain.User;
import lombok.Data;

@Data
public class DummyLoginRequest {
  private User.Role role;
}
