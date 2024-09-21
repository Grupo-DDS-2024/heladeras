package ar.edu.utn.dds.k3003.repositories;

import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import ar.edu.utn.dds.k3003.model.Temperatura;

public class TemperaturaMapper {

    public TemperaturaDTO map(Temperatura temperatura) {
        return new TemperaturaDTO(temperatura.getTemperatura(), temperatura.getHeladeraId(), temperatura.getFechaMedicion());
    }

}
