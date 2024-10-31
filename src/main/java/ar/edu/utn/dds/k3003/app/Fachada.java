package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.FachadaHeladeras;
import ar.edu.utn.dds.k3003.facades.FachadaViandas;
import ar.edu.utn.dds.k3003.facades.dtos.*;
import ar.edu.utn.dds.k3003.model.ColaboradoresSuscritos;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.model.Temperatura;
import ar.edu.utn.dds.k3003.repositories.*;
import ar.edu.utn.dds.k3003.utils.MQUtils;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.NotFoundResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import lombok.Getter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.util.*;

public class Fachada implements FachadaHeladeras {

    private HeladeraJPARepository heladeraRepository;
    private SuscripcionesRepository suscripcionesRepository;

    @Getter
    private EntityManagerFactory entityManagerFactory;

    private HeladeraMapper heladeraMapper;
    private TemperaturaMapper temperaturaMapper;
    private FachadaViandas fachadaViandas;
    private MQUtils cola_notificaciones;
    private MQUtils cola_incidentes;
    private StepMeterRegistry registro;

    public Fachada() {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("fachada_heladeras");
        this.heladeraRepository = new HeladeraJPARepository(entityManagerFactory.createEntityManager(), entityManagerFactory);
        this.suscripcionesRepository = new SuscripcionesRepository(entityManagerFactory);
        this.heladeraMapper = new HeladeraMapper();
        this.temperaturaMapper = new TemperaturaMapper();

    }

    public Fachada(HeladeraJPARepository heladeraRepository, HeladeraMapper heladeraMapper,
                   TemperaturaMapper temperaturaMapper, MQUtils cola_notificaciones, MQUtils cola_incidentes,StepMeterRegistry registro) {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("fachada_heladeras");
        this.heladeraRepository = heladeraRepository;
        heladeraRepository.setEntityManager(entityManagerFactory.createEntityManager());
        this.heladeraMapper = heladeraMapper;
        this.temperaturaMapper = temperaturaMapper;
        this.suscripcionesRepository = new SuscripcionesRepository(entityManagerFactory);
        this.cola_notificaciones = cola_notificaciones;
        this.cola_incidentes = cola_incidentes;
        this.registro=registro;
    }

    public Fachada(HeladeraJPARepository heladeraRepository, HeladeraMapper heladeraMapper,
                   TemperaturaMapper temperaturaMapper, EntityManagerFactory entityManagerFactory,
                   MQUtils cola_notificaciones, MQUtils cola_incidentes, StepMeterRegistry registro) {
        this.entityManagerFactory = entityManagerFactory;
        this.heladeraRepository = heladeraRepository;
        heladeraRepository.setEntityManagerFactory(entityManagerFactory);
        heladeraRepository.setEntityManager(entityManagerFactory.createEntityManager());
        this.heladeraMapper = heladeraMapper;
        this.temperaturaMapper = temperaturaMapper;
        this.suscripcionesRepository = new SuscripcionesRepository(entityManagerFactory);
        this.cola_notificaciones = cola_notificaciones;
        this.cola_incidentes = cola_incidentes;
        this.registro=registro;
    }


    @Override
    public HeladeraDTO agregar(HeladeraDTO heladeraDTO) {

        Heladera heladera = new Heladera(heladeraDTO.getNombre(), heladeraDTO.getCantidadDeViandas());
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
            this.reportarError(e);
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
        notificarFaltanNViandas(heladera.getId(), heladera.getCantidadDeViandas() - heladera.getCantidadViandas() , cola_notificaciones);

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
        notificarQuedanNViandas(heladera.getId(), heladera.getCantidadViandas(), this.cola_notificaciones);
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
        if(temperatura.getTemperatura() > 10){
            System.out.println("falla temperatura");
            heladera.marcarInactiva();
            reportarIncidente(heladera.getId(),1);
            this.notificarDesperfecto(heladera.getId(), this.cola_notificaciones);
        }
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

    public void agregarSuscriptor(Long colaborador_id, Integer heladera_id, int cantMinima, int viandasDisponibles, boolean notificarDesperfecto){
        EntityManager em = this.entityManagerFactory.createEntityManager();
        this.heladeraRepository.setEntityManager(em);
        em.getTransaction().begin();
        Heladera heladera = this.heladeraRepository.findById(heladera_id);
        ColaboradoresSuscritos colaborador = new ColaboradoresSuscritos(colaborador_id,heladera.getId(),cantMinima,viandasDisponibles,notificarDesperfecto);
        heladera.getColaboradoresSuscritos().add(colaborador);
        //suscripcionesRepository.save(colaborador);
        em.merge(heladera);
        em.getTransaction().commit();
        em.close();

    }


    public void notificarQuedanNViandas(int heladera_id, int cantViandas, MQUtils queue){

        Heladera heladera = this.heladeraRepository.findById(heladera_id);
        List<ColaboradoresSuscritos> suscriptores = heladera.getColaboradoresSuscritos();
        System.out.println("Suscriptores: " + suscriptores.size());
        suscriptores.forEach( s -> {
            System.out.println("Afuera del if: " + s.getColaborador_id());
            if(s.getCantMinima() != -1 && s.getCantMinima() >= cantViandas){
                System.out.println("S.cantMinima: " + s.getCantMinima());
                System.out.println("S.colaboradorId: " + s.getColaborador_id());
                Map<String, Object> response = new HashMap<>();
                response.put("tipo", 0);
                response.put("heladera_id",heladera_id);
                response.put("colaborador_id",s.getColaborador_id());

                try {
                    //ObjectMapper objectMapper = new ObjectMapper();
                    //String jsonMessage = objectMapper.writeValueAsString(response);
                    //queue.publish(jsonMessage);
                    System.out.println("RESPONSE: " + response.toString());
                    queue.publish(response.toString());

                } catch (IOException e) {
                    this.reportarError(e);
                    throw new RuntimeException(e);
                }
            }
                }
        );

    }

    public void notificarFaltanNViandas(int heladera_id, int viandasFaltantes, MQUtils queue){
        Heladera heladera = this.heladeraRepository.findById(heladera_id);
        List<ColaboradoresSuscritos> suscriptores = heladera.getColaboradoresSuscritos();
        suscriptores.forEach(s -> {
            if(s.getViandasDisponibles() != -1 && s.getViandasDisponibles() >= viandasFaltantes){
                Map<String, Object> response = new HashMap<>();
                response.put("colaborador_id",s.getColaborador_id());
                response.put("heladera_id",heladera_id);
                response.put("tipo",1);
                try {
                    queue.publish(response.toString());
                } catch (IOException e) {
                    this.reportarError(e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void notificarDesperfecto(int heladera_id, MQUtils queue){
        Heladera heladera = this.heladeraRepository.findById(heladera_id);
        List<ColaboradoresSuscritos> suscriptores = heladera.getColaboradoresSuscritos();
        suscriptores.forEach(s -> {
            if(s.isNotificarDesperfecto()){
                Map<String, Object> response = new HashMap<>();
                response.put("colaborador_id",s.getColaborador_id());
                response.put("heladera_id",heladera_id);
                response.put("tipo",2);
                try {
                    queue.publish(response.toString());
                } catch (IOException e) {
                    this.reportarError(e);
                    throw new RuntimeException(e);
                }
            }
        });
    }


    

    public HeladeraDTO buscarXId(Integer heladeraId) throws NoSuchElementException {
        return this.heladeraMapper.map(this.heladeraRepository.findById(heladeraId));
    }

    public EntityManager getEntityManager() {
        return this.heladeraRepository.getEntityManager();
    }

    public void fraude(int heladeraId) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        this.heladeraRepository.setEntityManager(em);
        em.getTransaction().begin();
        Heladera heladera = heladeraRepository.findById(heladeraId);
        heladera.marcarInactiva();
        reportarIncidente(heladera.getId(),1);
        this.notificarDesperfecto(heladera.getId(), this.cola_notificaciones);
        heladeraRepository.save(heladera);
        em.getTransaction().commit();
        em.close();
    }

    public void marcarInactiva(int heladeraId) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        this.heladeraRepository.setEntityManager(em);
        em.getTransaction().begin();
        Heladera heladera = heladeraRepository.findById(heladeraId);
        heladera.marcarInactiva();
        this.notificarDesperfecto(heladera.getId(), this.cola_notificaciones);
        heladeraRepository.save(heladera);
        em.getTransaction().commit();
        em.close();
    }

    public void reportarIncidente(int heladeraId, int tipo) {
        Map<String, Object> response = new HashMap<>();
        response.put("tipo", tipo);
        response.put("heladera_id",heladeraId);

        try {
            System.out.println("RESPONSE: " + response.toString());
            this.cola_incidentes.publish(response.toString());

        } catch (IOException e) {
            this.reportarError(e);
            throw new RuntimeException(e);

        }
    }

    public void arreglarHeladera(int heladeraId) {
        EntityManager em = this.entityManagerFactory.createEntityManager();
        this.heladeraRepository.setEntityManager(em);
        em.getTransaction().begin();
        Heladera heladera = heladeraRepository.findById(heladeraId);
        heladera.marcarActiva();
        heladeraRepository.save(heladera);
        em.getTransaction().commit();
        em.close();
    }
    public void reportarError(Exception e){
        Counter errorCounter = registro.counter("ddsHeladeras.errores","tipo_de_error",e.getClass().getSimpleName());
        errorCounter.increment();
    }

//    public Heladera buscarPorId(int heladeraId) {
//        Heladera heladera = null;
//        try {
//            heladera = heladeraRepository.findById(heladeraId);
//        } catch (Exception e) {
//            reportarError(e);
//        }
//        return heladera;
//    }
}
