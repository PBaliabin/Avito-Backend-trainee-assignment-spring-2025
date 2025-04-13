package com.avito.pvzservice.controller;


import com.avito.pvzservice.model.request.AddProductRequest;
import com.avito.pvzservice.model.response.AddProductResponse;
import com.avito.pvzservice.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ProductController {
  private ProductService productService;

  @PostMapping("/products")
  public ResponseEntity<AddProductResponse> addProduct(@RequestBody AddProductRequest addProductRequest, @RequestHeader("Authorization") String token) {
    return productService.addProduct(addProductRequest, token);
  }
}
