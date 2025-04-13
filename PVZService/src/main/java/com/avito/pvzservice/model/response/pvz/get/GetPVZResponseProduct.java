package com.avito.pvzservice.model.response.pvz.get;

import com.avito.pvzservice.domain.PVZ;
import com.avito.pvzservice.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class GetPVZResponseProduct {
    private UUID id;
    private Instant dateTime;
    private Product.Type type;
    private UUID receptionId;


}
