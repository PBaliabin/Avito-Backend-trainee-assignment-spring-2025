package com.avito.pvzservice.service;

import com.avito.pvzservice.model.request.AddProductRequest;
import com.avito.pvzservice.model.request.CreateReceptionRequest;
import com.avito.pvzservice.model.response.AddProductResponse;
import com.avito.pvzservice.model.response.CreateReceptionResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface ProductService {
  @NotNull
  ResponseEntity<AddProductResponse> addProduct(@NotNull AddProductRequest addProductRequest, @NotNull String token);
}
