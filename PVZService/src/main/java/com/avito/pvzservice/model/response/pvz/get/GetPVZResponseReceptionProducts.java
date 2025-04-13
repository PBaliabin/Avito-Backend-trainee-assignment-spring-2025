package com.avito.pvzservice.model.response.pvz.get;

import com.avito.pvzservice.domain.PVZ;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class GetPVZResponseReceptionProducts {
    private GetPVZResponseReception reception;
    private List<GetPVZResponseProduct> products;
}
