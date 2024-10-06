package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.step.StepMeterRegistry;

import java.util.HashMap;
import java.util.Map;

public class AgregarHeladeraController {
    private Fachada fachadaHeladeras;
    private StepMeterRegistry stepMeterRegistry;
    private Counter contadorHeladeras;

    public AgregarHeladeraController(Fachada fachadaHeladeras, StepMeterRegistry stepMeterRegistry) {
        super();
        this.fachadaHeladeras = fachadaHeladeras;
        this.stepMeterRegistry = stepMeterRegistry;
        this.contadorHeladeras = stepMeterRegistry.counter("ddsHeladeras.heladerasCreadas");
    }

    public void agregar(Context context) throws BadRequestResponse {
        try {
            HeladeraDTO heladeraDTO = context.bodyAsClass(HeladeraDTO.class);
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Heladera agregada correctamente");
            response.put("Heladera", this.fachadaHeladeras.agregar(heladeraDTO));
            contadorHeladeras.increment();
            context.status(200).json(response);
        } catch (Exception e) {
            throw new BadRequestResponse("Error de solicitud.");
        }

    }
}