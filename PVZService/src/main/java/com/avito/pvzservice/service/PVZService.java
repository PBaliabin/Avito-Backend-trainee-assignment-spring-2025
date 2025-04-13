package com.avito.pvzservice.service;

import com.avito.pvzservice.model.request.pvz.CreatePVZRequest;
import com.avito.pvzservice.model.response.pvz.CloseLastReceptionResponse;
import com.avito.pvzservice.model.response.pvz.CreatePVZResponse;
import com.avito.pvzservice.model.response.pvz.get.GetPVZResponseEntry;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public interface PVZService {
  @NotNull
  ResponseEntity<CreatePVZResponse> createPVZ(@NotNull CreatePVZRequest createPVZRequest, @NotNull String token);

  @NotNull
  ResponseEntity<List<GetPVZResponseEntry>> getPVZPageable(
          @NotNull Instant startDate,
          @NotNull Instant endDate,
          @NotNull Integer page,
          @NotNull Integer size,
          @NotNull String token);

  @NotNull
  ResponseEntity<CloseLastReceptionResponse> closeLastReception(@NotNull UUID pvzId, @NotNull String token);

  @NotNull
  ResponseEntity<Void> deleteLastProduct(@NotNull UUID pvzId, @NotNull String token);
}
