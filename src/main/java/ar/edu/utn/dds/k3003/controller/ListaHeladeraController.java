package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import ar.edu.utn.dds.k3003.repositories.HeladeraMapper;
import ar.edu.utn.dds.k3003.repositories.HeladeraRepository;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.List;

public class ListaHeladeraController {
    HeladeraRepository heladeraRepository;
    HeladeraMapper heladeraMapper;
    public ListaHeladeraController(HeladeraRepository heladeraRepository, HeladeraMapper heladeraMapper) {
        super();
        this.heladeraRepository = heladeraRepository;
        this.heladeraMapper = heladeraMapper;
    }

    public void listarHeladeras(Context context) throws Exception {
        //context.result(mapper.map(heladeraRepository.getHeladeras().stream().toList().get(2)).toString());
        List<HeladeraDTO> heladeraDTOS = new ArrayList<>();
        heladeraRepository.all().stream().forEach(heladera -> heladeraDTOS.add(heladeraMapper.map(heladera)));
        context.json(heladeraDTOS);
    }
}
