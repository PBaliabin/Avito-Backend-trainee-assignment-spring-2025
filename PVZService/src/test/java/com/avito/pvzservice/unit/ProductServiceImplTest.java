package com.avito.pvzservice.unit;

import com.avito.pvzservice.domain.PVZ;
import com.avito.pvzservice.domain.Product;
import com.avito.pvzservice.domain.Reception;
import com.avito.pvzservice.domain.User;
import com.avito.pvzservice.exception.BadRequestException;
import com.avito.pvzservice.exception.UnauthorizedException;
import com.avito.pvzservice.jwt.JwtTokenService;
import com.avito.pvzservice.model.request.AddProductRequest;
import com.avito.pvzservice.model.response.AddProductResponse;
import com.avito.pvzservice.repository.PVZRepository;
import com.avito.pvzservice.repository.ProductRepository;
import com.avito.pvzservice.repository.ReceptionRepository;
import com.avito.pvzservice.service.implementation.ProductsServiceImpl;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest // Загружает Spring-контекст (реальные бины)
@Testcontainers
class ProductServiceImplTest {

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
  @MockBean private ProductRepository productRepository; // Мок базы данных

  @Autowired private ProductsServiceImpl productsService; // Внедряем настоящий сервис

  @Test
  void addProduct_success() {
    String validToken = "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee));

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

    AddProductRequest addProductRequest = new AddProductRequest();
    addProductRequest.setType(Product.Type.ELECTRONICS);
    addProductRequest.setPvzId(pvz.getUuid());

    when(pvzRepository.findById(pvz.getUuid())).thenReturn(Optional.of(pvz));
    when(receptionRepository.findLatestByPvzId(pvz.getUuid())).thenReturn(Optional.of(reception));
    when(productRepository.save(any(Product.class))).thenAnswer(inv -> product);

    ResponseEntity<AddProductResponse> response = productsService.addProduct(addProductRequest, validToken);

    assertNotNull(response.getBody());
    Assertions.assertEquals(response.getBody().getId(), product.getUuid());
    Assertions.assertEquals(response.getBody().getDateTime(), reception.getDateTime());
    Assertions.assertEquals(response.getBody().getReceptionId(), reception.getUuid());
    Assertions.assertEquals(response.getBody().getType(), product.getType());
  }

  @Test
  void addProduct_wrongToken() {
    assertThrows(UnauthorizedException.class, () -> productsService.addProduct(
            new AddProductRequest(),
            "wrong token"));
  }

  @Test
  void addProduct_invalidRole() {
    assertThrows(UnauthorizedException.class, () -> productsService.addProduct(
            new AddProductRequest(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.moderator))));
  }

  @Test
  void addProduct_noPVZ() {
    when(pvzRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(BadRequestException.class, () -> productsService.addProduct(
            new AddProductRequest(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee))));
  }

  @Test
  void addProduct_noReception() {
    when(pvzRepository.findById(any())).thenReturn(Optional.of(new PVZ()));
    when(receptionRepository.findLatestByPvzId(any())).thenReturn(Optional.empty());
    assertThrows(BadRequestException.class, () -> productsService.addProduct(
            new AddProductRequest(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee))));
  }

  @Test
  void addProduct_noOpenReception() {
    when(pvzRepository.findById(any())).thenReturn(Optional.of(new PVZ()));

    Reception reception = new Reception();
    reception.setStatus(Reception.Status.close);
    when(receptionRepository.findLatestByPvzId(any())).thenReturn(Optional.of(reception));

    assertThrows(BadRequestException.class, () -> productsService.addProduct(
            new AddProductRequest(),
            "Bearer " + jwtTokenService.generateToken(String.valueOf(User.Role.employee))));
  }
}
