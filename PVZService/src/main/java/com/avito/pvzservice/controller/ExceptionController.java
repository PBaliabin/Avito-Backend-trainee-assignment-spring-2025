package com.avito.pvzservice.controller;

import com.avito.pvzservice.exception.BadRequestException;
import com.avito.pvzservice.exception.InternalServerErrorException;
import com.avito.pvzservice.exception.UnauthorizedException;
import com.avito.pvzservice.model.response.exception.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ExceptionResponse> notFound(BadRequestException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ExceptionResponse(ex.getMessage()));
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ExceptionResponse> unauthorized(UnauthorizedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ExceptionResponse(ex.getMessage()));
  }

  @ExceptionHandler(InternalServerErrorException.class)
  public ResponseEntity<ExceptionResponse> internalServerError(InternalServerErrorException ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ExceptionResponse(ex.getMessage()));
  }
}
