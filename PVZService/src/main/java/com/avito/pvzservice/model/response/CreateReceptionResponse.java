package com.avito.pvzservice.model.response;

import com.avito.pvzservice.domain.Reception;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CreateReceptionResponse {
    private UUID id;
    private Instant dateTime;
    private UUID pvzId;
    private Reception.Status status;
}
