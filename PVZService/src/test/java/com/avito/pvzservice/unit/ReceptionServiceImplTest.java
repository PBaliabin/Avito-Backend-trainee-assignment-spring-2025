package com.avito.pvzservice.unit;

import com.avito.pvzservice.domain.PVZ;
import com.avito.pvzservice.domain.Reception;
import com.avito.pvzservice.domain.User;
import com.avito.pvzservice.exception.BadRequestException;
import com.avito.pvzservice.exception.UnauthorizedException;
import com.avito.pvzservice.jwt.JwtTokenService;
import com.avito.pvzservice.model.request.CreateReceptionRequest;
import com.avito.pvzservice.model.response.CreateReceptionResponse;
import com.avito.pvzservice.repository.PVZRepository;
import com.avito.pvzservice.repository.ReceptionRepository;
import com.avito.pvzservice.service.implementation.ReceptionServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest // Загружает Spring-контекст (реальные бины)
@Testcontainers
class ReceptionServiceImplTest {

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

  @Autowired private JwtTokenService jwtTokenService; // Настоящий Jwt сервис

  @MockBean private PVZRepository pvzRepository; // Мок базы данных
  @MockBean private ReceptionRepository receptionRepository; // Мок базы данных

  @Autowired private ReceptionServiceImpl receptionService; // Внедряем настоящий сервис

  @Test
  void createReception_success() {
    String validToken = "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee));

    PVZ pvz = new PVZ();
    pvz.setUuid(UUID.randomUUID());
    pvz.setRegistrationDate(Instant.now());
    pvz.setCity(PVZ.City.MOSCOW);

    CreateReceptionRequest createReceptionRequest = new CreateReceptionRequest();
    createReceptionRequest.setPvzId(pvz.getUuid());

    Reception reception = new Reception();
    reception.setUuid(UUID.randomUUID());
    reception.setDateTime(Instant.now());
    reception.setPvzUuid(pvz.getUuid());
    reception.setProducts(new ArrayList<>());
    reception.setStatus(Reception.Status.in_progress);

    when(pvzRepository.findById(pvz.getUuid())).thenReturn(Optional.of(pvz));
    when(receptionRepository.save(any(Reception.class))).thenAnswer(inv -> reception);

    ResponseEntity<CreateReceptionResponse> response = receptionService.createReception(createReceptionRequest, validToken);

    assertNotNull(response.getBody());
    Assertions.assertEquals(response.getBody().getId(), reception.getUuid());
    Assertions.assertEquals(response.getBody().getDateTime(), reception.getDateTime());
    Assertions.assertEquals(response.getBody().getPvzId(), pvz.getUuid());
    Assertions.assertEquals(response.getBody().getStatus(), reception.getStatus());
  }

  @Test
  void createReception_wrongToken() {
    assertThrows(UnauthorizedException.class, () -> receptionService.createReception(new CreateReceptionRequest(), "wrong token"));
  }

  @Test
  void createReception_invalidRole() {
    assertThrows(UnauthorizedException.class, () -> receptionService.createReception(
            new CreateReceptionRequest(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.moderator))));
  }

  @Test
  void createReception_PVZNotFound() {
    when(pvzRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(BadRequestException.class, () -> receptionService.createReception(
            new CreateReceptionRequest(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee))));
  }
}
