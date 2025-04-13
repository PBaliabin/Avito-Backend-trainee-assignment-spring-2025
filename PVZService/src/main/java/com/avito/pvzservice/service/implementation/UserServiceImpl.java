package com.avito.pvzservice.service.implementation;

import com.avito.pvzservice.domain.User;
import com.avito.pvzservice.exception.BadRequestException;
import com.avito.pvzservice.exception.InternalServerErrorException;
import com.avito.pvzservice.jwt.JwtTokenService;
import com.avito.pvzservice.model.request.auth.DummyLoginRequest;
import com.avito.pvzservice.model.request.auth.LoginRequest;
import com.avito.pvzservice.model.request.auth.RegisterRequest;
import com.avito.pvzservice.model.response.auth.RegisterResponse;
import com.avito.pvzservice.repository.UserRepository;
import com.avito.pvzservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor()
public class UserServiceImpl implements UserService {
  private UserRepository userRepository;

  private PasswordEncoder passwordEncoder;
  private JwtTokenService jwtTokenService;

  @Override
  public ResponseEntity<String> dummyLogin(DummyLoginRequest dummyLoginRequest) {
    return ResponseEntity.ok().body(jwtTokenService.generateToken(String.valueOf(dummyLoginRequest.getRole())));
  }

  @Override
  @Transactional
  public ResponseEntity<RegisterResponse> register(RegisterRequest registerRequest) {
      User user = userRepository.findByEmail(registerRequest.getEmail()).orElse(null);

      if (user != null) {
        throw new BadRequestException("Email already in use");
      }

      user = new User();
      user.setEmail(registerRequest.getEmail());
      user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
      user.setRole(registerRequest.getRole());
      User saved = userRepository.save(user);

      RegisterResponse registerResponse = new RegisterResponse();
      registerResponse.setId(saved.getUuid());
      registerResponse.setEmail(saved.getEmail());
      registerResponse.setRole(saved.getRole());

      return ResponseEntity.ok().body(registerResponse);
  }

  @Override
  public ResponseEntity<String> login(LoginRequest loginRequest) {
      User user = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);

      if (user == null) {
        throw new BadRequestException("User not found");
      }

      if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
        throw new BadRequestException("Invalid password");
      }

      return ResponseEntity.ok().body(jwtTokenService.generateToken(String.valueOf(user.getRole())));
  }
}
