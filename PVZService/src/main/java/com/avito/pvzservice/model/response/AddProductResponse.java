package com.avito.pvzservice.model.response;

import com.avito.pvzservice.domain.Product;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AddProductResponse {
    private UUID id;
    private Instant dateTime;
    private Product.Type type;
    private UUID receptionId;
}
