package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import ar.edu.utn.dds.k3003.repositories.HeladeraJPARepository;
import ar.edu.utn.dds.k3003.repositories.HeladeraMapper;
import ar.edu.utn.dds.k3003.repositories.HeladeraRepository;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class ListaHeladeraController {
    Fachada fachada;
    HeladeraMapper heladeraMapper;

    public ListaHeladeraController(Fachada fachada, HeladeraMapper heladeraMapper) {
        super();
        this.fachada = fachada;
        this.heladeraMapper = heladeraMapper;
    }

    public void listarHeladeras(Context context) throws Exception {
        //context.result(mapper.map(heladeraRepository.getHeladeras().stream().toList().get(2)).toString());
        context.json(fachada.all());
    }
}
