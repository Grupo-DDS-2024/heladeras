package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.clients.ViandasProxy;
import ar.edu.utn.dds.k3003.controller.*;
import ar.edu.utn.dds.k3003.repositories.HeladeraJPARepository;
import ar.edu.utn.dds.k3003.repositories.HeladeraMapper;
import ar.edu.utn.dds.k3003.repositories.HeladeraRepository;
import ar.edu.utn.dds.k3003.repositories.TemperaturaMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ar.edu.utn.dds.k3003.facades.dtos.Constants;
import io.javalin.Javalin;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class WebApp {

    public static void main(String[] args) {
        Integer port = Integer.parseInt(
                System.getProperty("port", "8080"));
        Javalin app = Javalin.create().start(port);
        app.get("/", ctx -> ctx.result("Hola Mundo"));

        // Start Cosas.
        HeladeraRepository heladeraRepository = new HeladeraRepository();
        HeladeraMapper heladeraMapper = new HeladeraMapper();
        TemperaturaMapper temperaturaMapper = new TemperaturaMapper();


        HeladeraJPARepository heladeraJPARepository = new HeladeraJPARepository();
        EntityManagerFactory entityManagerFactory = startEntityManagerFactory();
        heladeraJPARepository.setEntityManagerFactory(entityManagerFactory);

        // Si veo que en ningún controller uso directamente los repo o mapper (el mapper da igual), saco el constructor con todos los parámetros y lo dejo como new Fachada();
        Fachada fachadaHeladeras = new Fachada(heladeraJPARepository, heladeraMapper, temperaturaMapper, entityManagerFactory);
        var objectMapper = createObjectMapper();
        fachadaHeladeras.setViandasProxy(new ViandasProxy(objectMapper));





        // End Cosas.

        // Controllers.
        var agregarHeladeraController = new AgregarHeladeraController(fachadaHeladeras);
        var obtenerHeladeraController = new ObtenerHeladeraController(fachadaHeladeras, entityManagerFactory, heladeraJPARepository);
        var depositarViandaController = new DepositarViandaController(fachadaHeladeras);
        var listaHeladeraController = new ListaHeladeraController(heladeraRepository, heladeraMapper); // de test nomás. dsp borrarlo! -> y borrar Repo, Mapper, e iniciar solo Fachada() sin params.
        var retirarViandaController = new RetirarViandaController(fachadaHeladeras);
        var registrarTemperaturaController = new RegistrarTemperaturaController(fachadaHeladeras);
        var obtenerTemperaturasController = new ObtenerTemperaturasController(fachadaHeladeras);

        app.post("/heladeras", agregarHeladeraController::agregar);
        app.get("/heladeras/{id}", obtenerHeladeraController::obtenerHeladera);
        app.post("/depositos", depositarViandaController::depositarVianda);  // falta test en postman
        app.post("/retiros", retirarViandaController::retirarVianda);  // falta test en postman
        app.post("/temperaturas", registrarTemperaturaController::registrarTemperatura);
        app.get("/heladeras/{id}/temperaturas", obtenerTemperaturasController::obtenerTemperaturas);




        app.get("/listado", listaHeladeraController::listarHeladeras); // de test nomás. dsp borrarlo!

    }

    public static ObjectMapper createObjectMapper() {            // lo voy a usar dsp para setViandasProxy cdo tenga q usar Proxy y Retrofit creo.
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var sdf = new SimpleDateFormat(Constants.DEFAULT_SERIALIZATION_FORMAT, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        objectMapper.setDateFormat(sdf);
        return objectMapper;
    }

    public static EntityManagerFactory startEntityManagerFactory() {
// https://stackoverflow.com/questions/8836834/read-environment-variables-in-persistence-xml-file
        Map<String, String> env = System.getenv();
        Map<String, Object> configOverrides = new HashMap<String, Object>();
        String[] keys = new String[] { "javax.persistence.jdbc.url", "javax.persistence.jdbc.user",
                "javax.persistence.jdbc.password", "javax.persistence.jdbc.driver", "hibernate.hbm2ddl.auto",
                "hibernate.connection.pool_size", "hibernate.show_sql" };
        for (String key : keys) {
            if (env.containsKey(key)) {
                String value = env.get(key);
                configOverrides.put(key, value);
            }
        }
        return Persistence.createEntityManagerFactory("fachada_heladeras", configOverrides);
    }

}
