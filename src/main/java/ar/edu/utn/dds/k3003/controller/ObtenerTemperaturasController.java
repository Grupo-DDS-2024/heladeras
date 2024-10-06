package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.NoSuchElementException;

public class ObtenerTemperaturasController {
    private Fachada fachadaHeladeras;

    public ObtenerTemperaturasController(Fachada fachadaHeladeras) {
        this.fachadaHeladeras = fachadaHeladeras;
    }

    public void obtenerTemperaturas(Context context) throws NotFoundResponse {
        try {
            List<TemperaturaDTO> temperaturas = this.fachadaHeladeras.obtenerTemperaturas(Integer.parseInt(context.pathParam("id")));
            context.json(temperaturas);
            context.status(200);
        } catch (NoSuchElementException e) {
            EntityManager em = this.fachadaHeladeras.getEntityManager();
            em.getTransaction().commit();
            em.close();
            throw new NotFoundResponse("Heladera no encontrada.");
        }
    }
}
