package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import ar.edu.utn.dds.k3003.repositories.HeladeraJPARepository;
import ar.edu.utn.dds.k3003.repositories.HeladeraMapper;
import ar.edu.utn.dds.k3003.repositories.HeladeraRepository;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class ListaHeladeraController {
    HeladeraJPARepository heladeraJPARepository;
    HeladeraMapper heladeraMapper;

    public ListaHeladeraController(HeladeraJPARepository heladeraJPARepository, HeladeraMapper heladeraMapper) {
        super();
        this.heladeraJPARepository = heladeraJPARepository;
        this.heladeraMapper = heladeraMapper;
    }

    public void listarHeladeras(Context context) throws Exception {
        //context.result(mapper.map(heladeraRepository.getHeladeras().stream().toList().get(2)).toString());
        List<HeladeraDTO> heladeraDTOS = new ArrayList<>();
        heladeraJPARepository.all().stream().forEach(heladera -> heladeraDTOS.add(heladeraMapper.map(heladera)));
        context.json(heladeraDTOS);
    }
}
