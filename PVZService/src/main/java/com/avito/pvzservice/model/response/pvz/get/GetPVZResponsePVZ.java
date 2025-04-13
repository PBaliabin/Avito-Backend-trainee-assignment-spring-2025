package com.avito.pvzservice.model.response.pvz.get;

import com.avito.pvzservice.domain.PVZ;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class GetPVZResponsePVZ {
    private UUID id;
    private Instant dateTime;
    private PVZ.City city;
}
