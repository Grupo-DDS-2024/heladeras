package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.*;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.model.Temperatura;
import ar.edu.utn.dds.k3003.repositories.HeladeraJPARepository;
import ar.edu.utn.dds.k3003.repositories.HeladeraMapper;
import ar.edu.utn.dds.k3003.repositories.HeladeraRepository;
import ar.edu.utn.dds.k3003.repositories.TemperaturaMapper;
import com.sun.net.httpserver.HttpsServer;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;
import lombok.Getter;
import org.hibernate.annotations.NotFound;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.Transactional;
import java.util.*;

public class Fachada implements FachadaHeladeras {

    private HeladeraJPARepository heladeraRepository;

    @Getter
    private EntityManagerFactory entityManagerFactory;

    private HeladeraMapper heladeraMapper;
    private TemperaturaMapper temperaturaMapper;
    private FachadaViandas fachadaViandas;

    public Fachada() {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("fachada_heladeras");
        this.heladeraRepository = new HeladeraJPARepository(entityManagerFactory.createEntityManager(), entityManagerFactory);
        this.heladeraMapper = new HeladeraMapper();
        this.temperaturaMapper = new TemperaturaMapper();

    }

    public Fachada(HeladeraJPARepository heladeraRepository, HeladeraMapper heladeraMapper, TemperaturaMapper temperaturaMapper) {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("fachada_heladeras");
        this.heladeraRepository = heladeraRepository;
        heladeraRepository.setEntityManager(entityManagerFactory.createEntityManager());
        this.heladeraMapper = heladeraMapper;
        this.temperaturaMapper = temperaturaMapper;
    }

    public Fachada(HeladeraJPARepository heladeraRepository, HeladeraMapper heladeraMapper, TemperaturaMapper temperaturaMapper, EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.heladeraRepository = heladeraRepository;
        heladeraRepository.setEntityManagerFactory(entityManagerFactory);
        heladeraRepository.setEntityManager(entityManagerFactory.createEntityManager());
        this.heladeraMapper = heladeraMapper;
        this.temperaturaMapper = temperaturaMapper;
    }


    @Override
    public HeladeraDTO agregar(HeladeraDTO heladeraDTO) {

        Heladera heladera = new Heladera(heladeraDTO.getNombre());
        EntityManager em = this.entityManagerFactory.createEntityManager();
        this.heladeraRepository.setEntityManager(em);
        em.getTransaction().begin();
        heladera = this.heladeraRepository.save(heladera);
        em.getTransaction().commit();
        em.close();

        return this.heladeraMapper.map(heladera);

    }

    @Override
    public void depositar(Integer heladeraId, String qrVianda) throws NoSuchElementException {

        EntityManager em = this.entityManagerFactory.createEntityManager();
        this.heladeraRepository.setEntityManager(em);
        em.getTransaction().begin();

        Heladera heladera = this.heladeraRepository.findById(heladeraId);

        ViandaDTO viandaDTO;
        try {
            viandaDTO = this.fachadaViandas.buscarXQR(qrVianda);
        } catch (NotFoundResponse e) {
            throw new NoSuchElementException();
        }

        // No funciona, ahora me deja depositar la misma vianda varias veces. Está bien en el componente de viandas?
        if (viandaDTO.getEstado().equals(EstadoViandaEnum.DEPOSITADA)) {
            throw new BadRequestResponse("La vianda ya está depositada en " + viandaDTO.getHeladeraId());
        }


        this.fachadaViandas.modificarEstado(viandaDTO.getCodigoQR(), EstadoViandaEnum.DEPOSITADA);
        this.fachadaViandas.modificarHeladera(viandaDTO.getCodigoQR(),heladeraId);

        heladera.guardar(qrVianda);
        em.persist(heladera);

        //this.heladeraRepository.getEntityManager().getTransaction().commit();
        em.getTransaction().commit();
        em.close();


    }

    @Override
    public Integer cantidadViandas(Integer heladeraId) throws NoSuchElementException {
        return this.heladeraRepository.findById(heladeraId).getCantidadViandas();
    }

    @Override
    public void retirar(RetiroDTO retiroDTO) throws NoSuchElementException {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        this.heladeraRepository.setEntityManager(em);
        em.getTransaction().begin();
        Heladera heladera = this.heladeraRepository.findById(retiroDTO.getHeladeraId());
        heladera.retirar(retiroDTO.getQrVianda());
        em.persist(heladera);
        ViandaDTO viandaDTO = this.fachadaViandas.buscarXQR(retiroDTO.getQrVianda());
        this.fachadaViandas.modificarEstado(viandaDTO.getCodigoQR(), EstadoViandaEnum.RETIRADA);
        em.getTransaction().commit();
        em.close();
    }

    @Override
    public void temperatura(TemperaturaDTO temperaturaDTO) {
        Temperatura temperatura = new Temperatura(temperaturaDTO.getTemperatura(), temperaturaDTO.getHeladeraId(), temperaturaDTO.getFechaMedicion());


        EntityManager em = this.entityManagerFactory.createEntityManager();
        this.heladeraRepository.setEntityManager(em);
        em.getTransaction().begin();
        Heladera heladera = this.heladeraRepository.findById(temperaturaDTO.getHeladeraId());

        heladera.setTemperatura(temperatura);
        this.heladeraRepository.save(heladera);

        em.getTransaction().commit();
        em.close();
    }

    @Override
    public List<TemperaturaDTO> obtenerTemperaturas(Integer heladeraId) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        heladeraRepository.setEntityManager(em);
        em.getTransaction().begin();
        Heladera heladera = this.heladeraRepository.findById(heladeraId);
        List<TemperaturaDTO> temperaturasHeladera = new ArrayList<>();
        heladera.getTemperaturas().forEach(temperatura -> temperaturasHeladera.add(this.temperaturaMapper.map(temperatura)));
        em.getTransaction().commit();
        em.close();
        return temperaturasHeladera;
    }

    public Collection<HeladeraDTO> all(){
        EntityManager em = this.entityManagerFactory.createEntityManager();
        heladeraRepository.setEntityManager(em);
        em.getTransaction().begin();
        List<HeladeraDTO> heladeras = heladeraRepository.all().stream().map(heladeraMapper::map).toList();
        em.getTransaction().commit();
        em.close();
        return heladeras;
    }

    @Override
    public void setViandasProxy(FachadaViandas fachadaViandas) {
        this.fachadaViandas = fachadaViandas;

    }

    public HeladeraDTO buscarXId(Integer heladeraId) throws NoSuchElementException {
        return this.heladeraMapper.map(this.heladeraRepository.findById(heladeraId));
    }

    public EntityManager getEntityManager() {
        return this.heladeraRepository.getEntityManager();
    }
}
