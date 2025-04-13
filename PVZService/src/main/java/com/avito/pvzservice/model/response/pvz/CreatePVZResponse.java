package com.avito.pvzservice.model.response.pvz;

import com.avito.pvzservice.domain.PVZ;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CreatePVZResponse {
    private UUID id;
    private Instant dateTime;
    private PVZ.City city;
}
