package com.avito.pvzservice.model.request.pvz;

import com.avito.pvzservice.domain.PVZ;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CreatePVZRequest {
    private UUID id;
    @JsonSerialize(using = InstantSerializer.class)
    private Instant registrationDate;
    private PVZ.City city;
}
