package com.avito.pvzservice.service;

import com.avito.pvzservice.model.request.CreateReceptionRequest;
import com.avito.pvzservice.model.response.CreateReceptionResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface ReceptionService {
  @NotNull
  ResponseEntity<CreateReceptionResponse> createReception(@NotNull CreateReceptionRequest createReceptionRequest, @NotNull String token);
}
