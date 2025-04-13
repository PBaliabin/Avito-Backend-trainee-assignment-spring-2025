package com.avito.pvzservice.service.implementation;

import com.avito.pvzservice.domain.PVZ;
import com.avito.pvzservice.domain.Reception;
import com.avito.pvzservice.domain.User;
import com.avito.pvzservice.exception.BadRequestException;
import com.avito.pvzservice.exception.InternalServerErrorException;
import com.avito.pvzservice.exception.UnauthorizedException;
import com.avito.pvzservice.jwt.JwtTokenService;
import com.avito.pvzservice.model.request.CreateReceptionRequest;
import com.avito.pvzservice.model.response.CreateReceptionResponse;
import com.avito.pvzservice.repository.PVZRepository;
import com.avito.pvzservice.repository.ReceptionRepository;
import com.avito.pvzservice.service.ReceptionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor()
public class ReceptionServiceImpl implements ReceptionService {
  private final ReceptionRepository receptionRepository;
  private final PVZRepository pvzRepository;

  private JwtTokenService jwtTokenService;


  @Override
  public ResponseEntity<CreateReceptionResponse> createReception(CreateReceptionRequest createReceptionRequest, String token) {
      if (!jwtTokenService.validateToken(token)) {
        throw new UnauthorizedException("Invalid or expired JWT token");
      }

      if (!Objects.equals(jwtTokenService.getRoleFromToken(token), String.valueOf(User.Role.employee))) {
        throw new UnauthorizedException("Invalid access role");
      }

      PVZ pvz = pvzRepository.findById(createReceptionRequest.getPvzId()).orElse(null);
      if (pvz == null) {
        throw new BadRequestException("PVZ not found");
      }

      Reception reception = new Reception();
      reception.setProducts(new ArrayList<>());
      reception.setPvzUuid(createReceptionRequest.getPvzId());
      reception.setStatus(Reception.Status.in_progress);

      Reception saved = receptionRepository.save(reception);

      CreateReceptionResponse createReceptionResponse = new CreateReceptionResponse();
      createReceptionResponse.setId(saved.getUuid());
      createReceptionResponse.setDateTime(saved.getDateTime());
      createReceptionResponse.setPvzId(saved.getPvzUuid());
      createReceptionResponse.setStatus(saved.getStatus());

      return ResponseEntity.ok().body(createReceptionResponse);
  }
}
