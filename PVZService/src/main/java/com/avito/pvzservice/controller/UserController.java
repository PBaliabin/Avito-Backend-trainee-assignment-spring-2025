package com.avito.pvzservice.controller;


import com.avito.pvzservice.model.request.auth.DummyLoginRequest;
import com.avito.pvzservice.model.request.auth.LoginRequest;
import com.avito.pvzservice.model.request.auth.RegisterRequest;
import com.avito.pvzservice.model.response.auth.RegisterResponse;
import com.avito.pvzservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class UserController {
  private UserService userService;

  @PostMapping("/dummyLogin")
  public ResponseEntity<String> dummyLogin(@RequestBody DummyLoginRequest loginRequest) {
    return userService.dummyLogin(loginRequest);
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
    return userService.register(registerRequest);
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
    return userService.login(loginRequest);
  }
}
