package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.dtos.RetiroDTO;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.step.StepMeterRegistry;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class RetirarViandaController {
    private Fachada fachadaHeladeras; // uso Fachada en vez de FachadaHeladeras para el getentitymanager..
    private StepMeterRegistry stepMeterRegistry;
    private Counter viandasRetiradas;
    public RetirarViandaController(Fachada fachadaHeladeras, StepMeterRegistry stepMeterRegistry) {
        this.fachadaHeladeras = fachadaHeladeras;
        this.stepMeterRegistry=stepMeterRegistry;
        this.viandasRetiradas=stepMeterRegistry.counter("ddsHeladeras.viandasRetiradas");
    }

    public void retirarVianda(Context context)  throws BadRequestResponse {
        try {
            RetiroDTO retiroDTO = context.bodyAsClass(RetiroDTO.class);
            this.fachadaHeladeras.retirar(retiroDTO);
            //context.status(200).result("Vianda retirada correctamente.");
            viandasRetiradas.increment();
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Vianda retirada correctamente");
            response.put("Retiro", retiroDTO);
            context.status(200).json(response);

        } catch (NoSuchElementException e) {
            EntityManager em = this.fachadaHeladeras.getEntityManager();
            em.getTransaction().commit();
            em.close();
            throw new BadRequestResponse("Error de solicitud.");
        }
    }
}
