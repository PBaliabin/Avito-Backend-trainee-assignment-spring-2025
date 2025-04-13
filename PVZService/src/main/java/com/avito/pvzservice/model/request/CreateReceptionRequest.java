package com.avito.pvzservice.model.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateReceptionRequest {
    private UUID pvzId;
}
