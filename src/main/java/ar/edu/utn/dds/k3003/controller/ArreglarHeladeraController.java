package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.model.ColaboradoresSuscritosDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public class ArreglarHeladeraController {

    Fachada fachadaHeladera;

    public ArreglarHeladeraController(Fachada fachada) {
        this.fachadaHeladera = fachada;
    }

    public void arreglarHeladera(Context context)  {
        try {
            this.fachadaHeladera.arreglarHeladera(Integer.parseInt(context.pathParam("id")));
        } catch (Exception e) {
            throw new BadRequestResponse("Error de solicitud.");
        }
    }
}
