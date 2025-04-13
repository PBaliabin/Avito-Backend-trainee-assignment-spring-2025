package com.avito.pvzservice.model.response.pvz.get;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GetPVZResponseEntry {
    private GetPVZResponsePVZ pvz;
    private List<GetPVZResponseReceptionProducts> receptions;
}
