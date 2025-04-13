package com.avito.pvzservice.model.request;

import com.avito.pvzservice.domain.Product;
import lombok.Data;

import java.util.UUID;

@Data
public class AddProductRequest {
    private Product.Type type;
    private UUID pvzId;
}
