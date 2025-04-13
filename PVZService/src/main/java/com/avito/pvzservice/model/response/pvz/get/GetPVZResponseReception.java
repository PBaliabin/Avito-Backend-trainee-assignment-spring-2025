package com.avito.pvzservice.model.response.pvz.get;

import com.avito.pvzservice.domain.Reception;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class GetPVZResponseReception {
    private UUID id;
    private Instant dateTime;
    private UUID pvzId;
    private Reception.Status status;
}
