package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

import javax.persistence.EntityManager;

public class FallaTecnicaController {
    Fachada fachadaHeladera;


    public FallaTecnicaController(Fachada fachada) {
        this.fachadaHeladera = fachada;
    }

    public void desperfecto(Context context) {
        try {
            int heladeraId = Integer.parseInt(context.pathParam("id"));
            this.fachadaHeladera.marcarInactiva(heladeraId);
            fachadaHeladera.reportarIncidente(heladeraId,0);
        } catch (Exception e) {
            throw new BadRequestResponse("Error de solicitud.");
        }

    }
}
