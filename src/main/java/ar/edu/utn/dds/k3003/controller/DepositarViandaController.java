package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoViandaEnum;
import ar.edu.utn.dds.k3003.facades.dtos.ViandaDTO;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DepositarViandaController {
    private Fachada fachadaHeladeras;
    public DepositarViandaController(Fachada fachadaHeladeras) {
        this.fachadaHeladeras = fachadaHeladeras;
    }

    public void depositarVianda(Context context) throws BadRequestResponse {
        try {
            ViandaDTO viandaDTO = context.bodyAsClass(ViandaDTO.class);
            this.fachadaHeladeras.depositar(viandaDTO.getHeladeraId(), viandaDTO.getCodigoQR());
            Map<String, Object> response = new HashMap<>();
            response.put("Mensaje", "Vianda depositada correctamente");
            response.put("Vianda", viandaDTO);
            response.put("Heladera", viandaDTO.getHeladeraId());

            context.status(200).json(response);

        } catch (NoSuchElementException e) {
            EntityManager em = this.fachadaHeladeras.getEntityManager();
            em.getTransaction().commit();
            em.close();
            throw new BadRequestResponse("Error de solicitud.");
        }
    }
}
