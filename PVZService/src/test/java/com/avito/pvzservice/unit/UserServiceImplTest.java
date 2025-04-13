package com.avito.pvzservice.unit;

import com.avito.pvzservice.domain.User;
import com.avito.pvzservice.exception.BadRequestException;
import com.avito.pvzservice.jwt.JwtTokenService;
import com.avito.pvzservice.model.request.auth.LoginRequest;
import com.avito.pvzservice.model.request.auth.RegisterRequest;
import com.avito.pvzservice.model.response.auth.RegisterResponse;
import com.avito.pvzservice.repository.UserRepository;
import com.avito.pvzservice.service.implementation.UserServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest // Загружает Spring-контекст (реальные бины)
@Testcontainers
class UserServiceImplTest {

  @Container
  public static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:11.1");

  static {
    postgreSQLContainer.start();
    System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
    System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
    System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
    System.setProperty(
        "spring.datasource.driver-class-name", postgreSQLContainer.getDriverClassName());
  }

  @AfterAll
  static void tearDown() {
    postgreSQLContainer.stop();
  }

  @Autowired private PasswordEncoder passwordEncoder; // Используем настоящий бин

  @Autowired private JwtTokenService jwtTokenService; // Настоящий Jwt сервис

  @MockBean private UserRepository userRepository; // Мок базы данных

  @Autowired private UserServiceImpl userService; // Внедряем настоящий сервис

  @Test
  void register_success() {
    String email = "test@mail.ru";
    RegisterRequest request = new RegisterRequest();
    request.setEmail(email);
    request.setPassword("password");
    request.setRole(User.Role.employee);

    UUID uuid = UUID.randomUUID();
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenAnswer(inv -> {
      User user = inv.getArgument(0);
      user.setUuid(uuid);
      return user;
    });

    ResponseEntity<RegisterResponse> response = userService.register(request);

    assertNotNull(response.getBody());
    Assertions.assertEquals(response.getBody().getId(), uuid);
    Assertions.assertEquals(response.getBody().getEmail(), request.getEmail());
    Assertions.assertEquals(response.getBody().getRole(), request.getRole());

    verify(userRepository, times(1)).findByEmail(email);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void register_existingEmail() {
    User user = new User();

    when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

    assertThrows(BadRequestException.class, () -> userService.register(new RegisterRequest()));
  }

  @Test
  void login_success() {
    String email = "test@mail.ru";
    String password = "password";
    LoginRequest request = new LoginRequest();
    request.setEmail(email);
    request.setPassword(password);

    User user = new User();
    user.setUuid(UUID.randomUUID());
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.setRole(User.Role.employee);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    ResponseEntity<String> responseEntity = userService.login(request);

    assertNotNull(responseEntity);
    assertTrue(jwtTokenService.validateToken("Bearer " + responseEntity.getBody()));
  }

  @Test
  void login_wrongEmail() {
    when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

    assertThrows(BadRequestException.class, () -> userService.login(new LoginRequest()));
  }

  @Test
  void login_wrongPassword() {
    LoginRequest request = new LoginRequest();
    request.setEmail("test@mail.ru");
    request.setPassword("wrongPassword");

    User user = new User();
    user.setUuid(UUID.randomUUID());
    user.setEmail("test@mail.ru");
    user.setPassword(passwordEncoder.encode("password"));
    user.setRole(User.Role.employee);

    when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

    assertThrows(BadRequestException.class, () -> userService.login(request));
  }
}
