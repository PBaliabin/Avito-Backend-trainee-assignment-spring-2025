package com.avito.pvzservice.controller;


import com.avito.pvzservice.model.request.CreateReceptionRequest;
import com.avito.pvzservice.model.response.CreateReceptionResponse;
import com.avito.pvzservice.service.ReceptionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class ReceptionController {
  private ReceptionService receptionService;

  @PostMapping("/receptions")
  public ResponseEntity<CreateReceptionResponse> createReception(@RequestBody CreateReceptionRequest createReceptionRequest, @RequestHeader("Authorization") String token) {
    return receptionService.createReception(createReceptionRequest, token);
  }
}
