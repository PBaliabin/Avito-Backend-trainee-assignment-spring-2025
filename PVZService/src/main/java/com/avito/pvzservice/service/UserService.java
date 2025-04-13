package com.avito.pvzservice.service;

import com.avito.pvzservice.model.request.auth.DummyLoginRequest;
import com.avito.pvzservice.model.request.auth.LoginRequest;
import com.avito.pvzservice.model.request.auth.RegisterRequest;
import com.avito.pvzservice.model.response.auth.RegisterResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
  @NotNull
  ResponseEntity<String> dummyLogin(@NotNull DummyLoginRequest dummyLoginRequest);

  @NotNull
  ResponseEntity<RegisterResponse> register(@NotNull RegisterRequest registerRequest);

  @NotNull
  ResponseEntity<String> login(@NotNull LoginRequest loginRequest);
}
