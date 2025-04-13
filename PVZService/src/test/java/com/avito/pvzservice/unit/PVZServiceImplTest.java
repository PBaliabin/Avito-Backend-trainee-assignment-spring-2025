package com.avito.pvzservice.unit;

import com.avito.pvzservice.domain.PVZ;
import com.avito.pvzservice.domain.Product;
import com.avito.pvzservice.domain.Reception;
import com.avito.pvzservice.domain.User;
import com.avito.pvzservice.exception.BadRequestException;
import com.avito.pvzservice.exception.UnauthorizedException;
import com.avito.pvzservice.jwt.JwtTokenService;
import com.avito.pvzservice.model.request.pvz.CreatePVZRequest;
import com.avito.pvzservice.model.response.pvz.CloseLastReceptionResponse;
import com.avito.pvzservice.model.response.pvz.CreatePVZResponse;
import com.avito.pvzservice.model.response.pvz.get.GetPVZResponseEntry;
import com.avito.pvzservice.repository.PVZRepository;
import com.avito.pvzservice.repository.ReceptionRepository;
import com.avito.pvzservice.service.implementation.PVZServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest // Загружает Spring-контекст (реальные бины)
@Testcontainers
class PVZServiceImplTest {

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

  @Autowired private PVZServiceImpl pvzService; // Внедряем настоящий сервис

  @Test
  void createPVZ_success() {
    CreatePVZRequest createPVZRequest = new CreatePVZRequest();
    createPVZRequest.setId(UUID.fromString("497f6eca-6276-4993-bfeb-53cbbbba6f08"));
    createPVZRequest.setRegistrationDate(Instant.parse("2019-08-24T14:15:22Z"));
    createPVZRequest.setCity(PVZ.City.fromString("Москва"));

    String validToken = "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.moderator));

    when(pvzRepository.findById(any())).thenReturn(Optional.empty());
    when(pvzRepository.save(any(PVZ.class))).thenAnswer(inv -> inv.<PVZ>getArgument(0));

    ResponseEntity<CreatePVZResponse> response = pvzService.createPVZ(createPVZRequest, validToken);

    assertNotNull(response.getBody());
    Assertions.assertEquals(response.getBody().getId(), createPVZRequest.getId());
    Assertions.assertEquals(response.getBody().getDateTime(), createPVZRequest.getRegistrationDate());
    Assertions.assertEquals(response.getBody().getCity(), createPVZRequest.getCity());
  }

  @Test
  void createPVZ_wrongToken() {
    assertThrows(UnauthorizedException.class, () -> pvzService.createPVZ(new CreatePVZRequest(), "wrong token"));
  }

  @Test
  void createPVZ_invalidRole() {
    assertThrows(UnauthorizedException.class, () -> pvzService.createPVZ(
            new CreatePVZRequest(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee))));
  }

  @Test
  void createPVZ_alreadyExists() {
    when(pvzRepository.findById(any())).thenReturn(Optional.of(new PVZ()));

    assertThrows(BadRequestException.class, () -> pvzService.createPVZ(
            new CreatePVZRequest(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.moderator))));
  }

  @Test
  void getPVZPageable_allEmpty() {
    Instant start = Instant.now();
    Instant end = Instant.now();
    Integer page = 1;
    Integer size = 10;

    when(receptionRepository.findByDateRange(start, end, PageRequest.of(page, size))).thenReturn(Page.empty());
    when(pvzRepository.findAllById(anyCollection())).thenReturn(new ArrayList<>());

    ResponseEntity<List<GetPVZResponseEntry>> response = pvzService.getPVZPageable(
            start, end, page, size,
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.moderator)));

    assertNotNull(response.getBody());
    assertTrue(response.getBody().isEmpty());
  }

  @Test
  void getPVZPageable_oneEach() {
    Instant start = Instant.now();
    Instant end = Instant.now();
    Integer page = 1;
    Integer size = 10;

    PVZ pvz = new PVZ();
    pvz.setUuid(UUID.randomUUID());
    pvz.setRegistrationDate(Instant.now());
    pvz.setCity(PVZ.City.MOSCOW);

    Product product = new Product();
    product.setUuid(UUID.randomUUID());
    product.setDateTime(Instant.now());
    product.setType(Product.Type.ELECTRONICS);

    Reception reception = new Reception();
    reception.setUuid(UUID.randomUUID());
    reception.setDateTime(Instant.now());
    reception.setPvzUuid(pvz.getUuid());
    reception.setProducts(new ArrayList<>());
    reception.addProduct(product);
    reception.setStatus(Reception.Status.in_progress);

    Page<Reception> mockedPage = new PageImpl<>(
            Collections.singletonList(reception),
            PageRequest.of(page, size),
            1L
    );
    when(receptionRepository.findByDateRange(start, end, PageRequest.of(page, size))).thenReturn(mockedPage);

    when(pvzRepository.findAllById(anyCollection())).thenReturn(List.of(pvz));

    ResponseEntity<List<GetPVZResponseEntry>> response = pvzService.getPVZPageable(
            start, end, page, size,
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.moderator)));

    assertNotNull(response.getBody());
    assertFalse(response.getBody().isEmpty());
  }

  @Test
  void getPVZPageable_wrongToken() {
    assertThrows(UnauthorizedException.class, () -> pvzService.getPVZPageable(
            Instant.now(),
            Instant.now(),
            1,
            1,
            "wrong token"));
  }

  @Test
  void closeLastReception_success() {
    UUID pvzId = UUID.randomUUID();
    String validToken = "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee));

    Reception reception = new Reception();
    reception.setUuid(UUID.randomUUID());
    reception.setDateTime(Instant.now());
    reception.setPvzUuid(pvzId);
    reception.setProducts(new ArrayList<>());
    reception.setStatus(Reception.Status.in_progress);

    when(receptionRepository.findLatestByPvzId(pvzId)).thenReturn(Optional.of(reception));
    when(receptionRepository.save(any(Reception.class))).thenAnswer(inv -> inv.<Reception>getArgument(0));

    ResponseEntity<CloseLastReceptionResponse> response = pvzService.closeLastReception(pvzId, validToken);

    assertNotNull(response.getBody());
    Assertions.assertEquals(response.getBody().getId(), reception.getUuid());
    Assertions.assertEquals(response.getBody().getDateTime(), reception.getDateTime());
    Assertions.assertEquals(response.getBody().getPvzId(), reception.getPvzUuid());
    Assertions.assertEquals(response.getBody().getStatus(), Reception.Status.in_progress);
  }

  @Test
  void closeLastReception_wrongToken() {
    assertThrows(UnauthorizedException.class, () -> pvzService.closeLastReception(
            UUID.randomUUID(),
            "wrong token"));
  }

  @Test
  void closeLastReception_invalidRole() {
    assertThrows(UnauthorizedException.class, () -> pvzService.closeLastReception(
            UUID.randomUUID(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.moderator))));
  }

  @Test
  void closeLastReception_noReception() {
    when(receptionRepository.findLatestByPvzId(any())).thenReturn(Optional.empty());
    assertThrows(BadRequestException.class, () -> pvzService.closeLastReception(
            UUID.randomUUID(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee))));
  }

  @Test
  void closeLastReception_noOpenReception() {
    Reception reception = new Reception();
    when(receptionRepository.findLatestByPvzId(any())).thenReturn(Optional.of(reception));
    reception.setStatus(Reception.Status.close);

    assertThrows(BadRequestException.class, () -> pvzService.closeLastReception(
            UUID.randomUUID(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee))));
  }

  @Test
  void deleteLastProduct_success() {
    UUID pvzId = UUID.randomUUID();
    String validToken = "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee));

    Reception reception = new Reception();
    reception.setUuid(UUID.randomUUID());
    reception.setDateTime(Instant.now());
    reception.setPvzUuid(pvzId);
    reception.setProducts(new ArrayList<>());
    reception.addProduct(new Product());
    reception.setStatus(Reception.Status.in_progress);

    when(receptionRepository.findLatestByPvzId(pvzId)).thenReturn(Optional.of(reception));
    when(receptionRepository.save(any(Reception.class))).thenAnswer(inv -> inv.<Reception>getArgument(0));

    ResponseEntity<Void> response = pvzService.deleteLastProduct(pvzId, validToken);

    assertEquals(response.getStatusCode(), HttpStatusCode.valueOf(200));
    assertEquals(reception.getProducts().size(), 0);
  }

  @Test
  void deleteLastProduct_wrongToken() {
    assertThrows(UnauthorizedException.class, () -> pvzService.deleteLastProduct(
            UUID.randomUUID(),
            "wrong token"));
  }

  @Test
  void deleteLastProduct_invalidRole() {
    assertThrows(UnauthorizedException.class, () -> pvzService.deleteLastProduct(
            UUID.randomUUID(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.moderator))));
  }

  @Test
  void deleteLastProduct_noReception() {
    when(receptionRepository.findLatestByPvzId(any())).thenReturn(Optional.empty());
    assertThrows(BadRequestException.class, () -> pvzService.deleteLastProduct(
            UUID.randomUUID(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee))));
  }

  @Test
  void deleteLastProduct_noOpenReception() {
    Reception reception = new Reception();
    when(receptionRepository.findLatestByPvzId(any())).thenReturn(Optional.of(reception));
    reception.setStatus(Reception.Status.close);

    assertThrows(BadRequestException.class, () -> pvzService.deleteLastProduct(
            UUID.randomUUID(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee))));
  }

  @Test
  void deleteLastProduct_noProductToDelete() {
    UUID pvzId = UUID.randomUUID();
    String validToken = "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee));

    Reception reception = new Reception();
    reception.setUuid(UUID.randomUUID());
    reception.setDateTime(Instant.now());
    reception.setPvzUuid(pvzId);
    reception.setProducts(new ArrayList<>());
    reception.setStatus(Reception.Status.in_progress);

    when(receptionRepository.findLatestByPvzId(pvzId)).thenReturn(Optional.of(reception));

    assertThrows(BadRequestException.class, () -> pvzService.deleteLastProduct(
            pvzId,
            validToken));
  }
}
