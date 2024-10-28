package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.HeladeraDTO;
import ar.edu.utn.dds.k3003.repositories.HeladeraJPARepository;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.NoSuchElementException;

public class ObtenerHeladeraController {

    private Fachada fachadaHeladera; // No le puedo poner FachadaHeladeras porque no tiene el buscarXId.
    private EntityManagerFactory entityManagerFactory;
    private HeladeraJPARepository heladeraRepository;

    public ObtenerHeladeraController(Fachada fachadaHeladera, EntityManagerFactory entityManagerFactory, HeladeraJPARepository heladeraRepository) {
        super();
        this.fachadaHeladera = fachadaHeladera;
        this.entityManagerFactory = entityManagerFactory;
        this.heladeraRepository = heladeraRepository;
    }


    public void obtenerHeladera(Context context) throws NotFoundResponse {
        //EntityManager em = this.entityManagerFactory.createEntityManager(); // esto está raro creado acá, debería estar en fachada, y dsp hacer un get
        //this.heladeraRepository.setEntityManager(em);
        //em.getTransaction().begin();
        try {


            HeladeraDTO heladeraDTO = this.fachadaHeladera.buscarXId((Integer.parseInt(context.pathParam("id"))));

            context.status(200).json(heladeraDTO); // Falta agregar la cantidad de viandas depositadas actualmente

        } catch (NoSuchElementException e) {
            throw new NotFoundResponse("Heladera no encontrada.");
        }
    }
}
