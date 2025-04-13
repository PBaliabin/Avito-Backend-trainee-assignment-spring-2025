package com.avito.pvzservice.integration;

import com.avito.pvzservice.domain.PVZ;
import com.avito.pvzservice.domain.Product;
import com.avito.pvzservice.domain.Reception;
import com.avito.pvzservice.domain.User;
import com.avito.pvzservice.model.request.AddProductRequest;
import com.avito.pvzservice.model.request.CreateReceptionRequest;
import com.avito.pvzservice.model.request.auth.DummyLoginRequest;
import com.avito.pvzservice.model.request.pvz.CreatePVZRequest;
import com.avito.pvzservice.repository.PVZRepository;
import com.avito.pvzservice.repository.ReceptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class PVZIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:11.1");

    static {
        postgreSQLContainer.start();
        // Устанавливаем параметры подключения через System.setProperty
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PVZRepository pvzRepository;

    @Autowired
    private ReceptionRepository receptionRepository;

    @Test
    void integrationTest() throws Exception {
        // tokens
        DummyLoginRequest dummyLoginRequest = new DummyLoginRequest();

        dummyLoginRequest.setRole(User.Role.employee);
        String employeeToken = mockMvc.perform(
                        post("/dummyLogin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(dummyLoginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        dummyLoginRequest.setRole(User.Role.moderator);
        String moderatorToken = mockMvc.perform(
                        post("/dummyLogin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(dummyLoginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 1. Создание нового ПВЗ
        CreatePVZRequest createPVZRequest = new CreatePVZRequest();
        createPVZRequest.setId(UUID.fromString("497f6eca-6276-4993-bfeb-53cbbbba6f08"));
        createPVZRequest.setRegistrationDate(Instant.parse("2019-08-24T14:15:22Z"));
        createPVZRequest.setCity(PVZ.City.MOSCOW);

        mockMvc.perform(post("/pvz")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(createPVZRequest))
                        .header("Authorization", "Bearer " + moderatorToken))
                .andExpect(status().isOk());

        PVZ savedPvz = pvzRepository.findById(createPVZRequest.getId()).orElse(null);
        assertNotNull(savedPvz);

        CreateReceptionRequest createReceptionRequest = new CreateReceptionRequest();
        createReceptionRequest.setPvzId(createPVZRequest.getId());
        mockMvc.perform(post("/receptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(createReceptionRequest))
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());

        Reception savedReception = receptionRepository.findLatestByPvzId(createPVZRequest.getId()).orElse(null);
        assertNotNull(savedReception);

        AddProductRequest addProductRequest = new AddProductRequest();
        addProductRequest.setPvzId(createPVZRequest.getId());
        addProductRequest.setType(Product.Type.CLOTHING);
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(post("/products")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(addProductRequest))
                            .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk());
        }

        savedReception = receptionRepository.findLatestByPvzId(createPVZRequest.getId()).orElse(null);
        assertNotNull(savedReception);
        assertEquals(savedReception.getProducts().size(), 50);

        mockMvc.perform(post("/pvz/{pvzId}/close_last_reception", createPVZRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());

        savedReception = receptionRepository.findLatestByPvzId(createPVZRequest.getId()).orElse(null);
        assertNotNull(savedReception);
        assertEquals(savedReception.getStatus(), Reception.Status.close);
    }
}
