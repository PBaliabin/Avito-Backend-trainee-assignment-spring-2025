package com.avito.pvzservice.service.implementation;

import com.avito.pvzservice.domain.PVZ;
import com.avito.pvzservice.domain.Reception;
import com.avito.pvzservice.domain.User;
import com.avito.pvzservice.exception.BadRequestException;
import com.avito.pvzservice.exception.InternalServerErrorException;
import com.avito.pvzservice.exception.UnauthorizedException;
import com.avito.pvzservice.jwt.JwtTokenService;
import com.avito.pvzservice.model.request.pvz.CreatePVZRequest;
import com.avito.pvzservice.model.response.pvz.CloseLastReceptionResponse;
import com.avito.pvzservice.model.response.pvz.CreatePVZResponse;
import com.avito.pvzservice.model.response.pvz.get.*;
import com.avito.pvzservice.repository.PVZRepository;
import com.avito.pvzservice.repository.ReceptionRepository;
import com.avito.pvzservice.service.PVZService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor()
public class PVZServiceImpl implements PVZService {
  private final ReceptionRepository receptionRepository;
  private final PVZRepository pvzRepository;

  private JwtTokenService jwtTokenService;


  @Override
  public ResponseEntity<CreatePVZResponse> createPVZ(CreatePVZRequest createPVZRequest, String token) {
      if (!jwtTokenService.validateToken(token)) {
        throw new UnauthorizedException("Invalid or expired JWT token");
      }

      if (!Objects.equals(jwtTokenService.getRoleFromToken(token), String.valueOf(User.Role.moderator))) {
        throw new UnauthorizedException("Invalid access role");
      }

      PVZ pvz = pvzRepository.findById(createPVZRequest.getId()).orElse(null);
      if (pvz != null) {
        throw new BadRequestException("PVZ already exists");
      }

      pvz = new PVZ();
      pvz.setUuid(createPVZRequest.getId());
      pvz.setRegistrationDate(createPVZRequest.getRegistrationDate());
      pvz.setCity(createPVZRequest.getCity());

      PVZ saved = pvzRepository.save(pvz);

      CreatePVZResponse createPVZResponse = new CreatePVZResponse();
      createPVZResponse.setId(saved.getUuid());
      createPVZResponse.setDateTime(saved.getRegistrationDate());
      createPVZResponse.setCity(saved.getCity());

      return ResponseEntity.ok(createPVZResponse);
  }

  @Override
  public ResponseEntity<List<GetPVZResponseEntry>> getPVZPageable(Instant startDate, Instant endDate, Integer page, Integer size, String token) {
      if (!jwtTokenService.validateToken(token)) {
        throw new UnauthorizedException("Invalid or expired JWT token");
      }

      Page<Reception> receptionsPage = receptionRepository.findByDateRange(
              startDate,
              endDate,
              PageRequest.of(page, size));

      Map<UUID, List<Reception>> receptionsByPvz = receptionsPage
              .getContent()
              .stream()
              .collect(Collectors.groupingBy(Reception::getPvzUuid));

      List<GetPVZResponseEntry> getPVZResponseEntries = new ArrayList<>();

      List<PVZ> pvzList = pvzRepository.findAllById(receptionsByPvz.keySet());
      for (PVZ pvz : pvzList) {
        GetPVZResponsePVZ getPVZResponsePVZ = new GetPVZResponsePVZ(
                pvz.getUuid(),
                pvz.getRegistrationDate(),
                pvz.getCity()
        );

        List<GetPVZResponseReceptionProducts> getPVZResponseReceptionProducts = new ArrayList<>();

        List<Reception> receptions = receptionsByPvz.get(pvz.getUuid());
        for (Reception reception : receptions) {
          GetPVZResponseReception getPVZResponseReception = new GetPVZResponseReception(
                  reception.getUuid(),
                  reception.getDateTime(),
                  reception.getPvzUuid(),
                  reception.getStatus()
          );

          List<GetPVZResponseProduct> getPVZResponseProducts = reception.getProducts().stream()
                  .map(product -> new GetPVZResponseProduct(
                          product.getUuid(),
                          product.getDateTime(),
                          product.getType(),
                          reception.getUuid()
                  ))
                  .toList();

          getPVZResponseReceptionProducts.add(new GetPVZResponseReceptionProducts(getPVZResponseReception, getPVZResponseProducts));
        }

        getPVZResponseEntries.add(new GetPVZResponseEntry(getPVZResponsePVZ, getPVZResponseReceptionProducts));
      }

      return ResponseEntity.ok().body(getPVZResponseEntries);
  }

  @Override
  @Transactional
  public ResponseEntity<CloseLastReceptionResponse> closeLastReception(UUID pvzId, String token) {
      if (!jwtTokenService.validateToken(token)) {
        throw new UnauthorizedException("Invalid or expired JWT token");
      }

      if (!Objects.equals(jwtTokenService.getRoleFromToken(token), String.valueOf(User.Role.employee))) {
        throw new UnauthorizedException("Invalid access role");
      }

      Reception reception = receptionRepository.findLatestByPvzId(pvzId).orElse(null);
      if (reception == null) {
        throw new BadRequestException("Reception not found");
      }

      if (reception.getStatus() == Reception.Status.close){
        throw new BadRequestException("Reception is already closed");
      }

      CloseLastReceptionResponse closeLastReceptionResponse = new CloseLastReceptionResponse();
      closeLastReceptionResponse.setId(reception.getUuid());
      closeLastReceptionResponse.setDateTime(reception.getDateTime());
      closeLastReceptionResponse.setPvzId(reception.getPvzUuid());
      closeLastReceptionResponse.setStatus(reception.getStatus());

      reception.setStatus(Reception.Status.close);
      receptionRepository.save(reception);

      return ResponseEntity.ok().body(closeLastReceptionResponse);
  }

  @Override
  @Transactional
  public ResponseEntity<Void> deleteLastProduct(UUID pvzId, String token) {
      if (!jwtTokenService.validateToken(token)) {
        throw new UnauthorizedException("Invalid or expired JWT token");
      }

      if (!Objects.equals(jwtTokenService.getRoleFromToken(token), String.valueOf(User.Role.employee))) {
        throw new UnauthorizedException("Invalid access role");
      }

      Reception reception = receptionRepository.findLatestByPvzId(pvzId).orElse(null);
      if (reception == null) {
        throw new BadRequestException("Reception not found");
      }

      if (reception.getStatus() == Reception.Status.close){
        throw new BadRequestException("All receptions are already closed");
      }

      if (reception.getProducts().isEmpty()){
        throw new BadRequestException("No product to delete");
      }

      reception.removeLastProduct();
      receptionRepository.save(reception);

      return ResponseEntity.ok().build();
  }
}
