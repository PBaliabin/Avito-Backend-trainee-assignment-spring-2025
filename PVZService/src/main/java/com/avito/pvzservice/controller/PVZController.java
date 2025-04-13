package com.avito.pvzservice.controller;


import com.avito.pvzservice.model.request.pvz.CreatePVZRequest;
import com.avito.pvzservice.model.response.pvz.CloseLastReceptionResponse;
import com.avito.pvzservice.model.response.pvz.CreatePVZResponse;
import com.avito.pvzservice.model.response.pvz.get.GetPVZResponseEntry;
import com.avito.pvzservice.service.PVZService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pvz")
@AllArgsConstructor
public class PVZController {
  private PVZService pvzService;

  @PostMapping
  public ResponseEntity<CreatePVZResponse> createPVZ(@RequestBody CreatePVZRequest createPVZRequest, @RequestHeader("Authorization") String token) {
    return pvzService.createPVZ(createPVZRequest, token);
  }

  @GetMapping
  public ResponseEntity<List<GetPVZResponseEntry>> getPVZPageable(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
                                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestHeader("Authorization") String token) {
    Instant start = startDate != null ? startDate : Instant.EPOCH;
    Instant end = endDate != null ? endDate : Instant.now();

    return pvzService.getPVZPageable(start, end, page, size, token);
  }

  @PostMapping("/{pvzId}/close_last_reception")
  public ResponseEntity<CloseLastReceptionResponse> closeLastReception(@PathVariable UUID pvzId, @RequestHeader("Authorization") String token) {
    return pvzService.closeLastReception(pvzId, token);
  }

  @PostMapping("/{pvzId}/delete_last_product")
  public ResponseEntity<Void> deleteLastProduct(@PathVariable UUID pvzId, @RequestHeader("Authorization") String token) {
    return pvzService.deleteLastProduct(pvzId, token);
  }
}
