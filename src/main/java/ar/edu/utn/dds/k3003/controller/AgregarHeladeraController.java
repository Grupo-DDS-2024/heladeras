package ar.edu.utn.dds.k3003.controller;
import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public class AgregarHeladeraController {
    private Fachada fachadaHeladeras;
    public AgregarHeladeraController(Fachada fachadaHeladeras) {
        super();
        this.fachadaHeladeras = fachadaHeladeras;
    }

    public void agregar(Context context) throws BadRequestResponse {
        try {
            HeladeraDTO heladeraDTO = context.bodyAsClass(HeladeraDTO.class);
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Heladera agregada correctamente");
            response.put("Heladera", this.fachadaHeladeras.agregar(heladeraDTO));
            context.status(200).json(response);
        } catch (Exception e) {
            throw new BadRequestResponse("Error de solicitud.");
        }

    }
}