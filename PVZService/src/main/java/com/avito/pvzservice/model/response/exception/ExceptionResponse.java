package com.avito.pvzservice.model.response.exception;

import lombok.Data;

@Data
public class ExceptionResponse {
  String message;

  public ExceptionResponse(String errors) {
    this.message = errors;
  }
}
