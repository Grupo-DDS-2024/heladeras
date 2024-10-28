package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.clients.ViandasProxy;
import ar.edu.utn.dds.k3003.controller.*;
import ar.edu.utn.dds.k3003.facades.dtos.Constants;
import ar.edu.utn.dds.k3003.repositories.HeladeraJPARepository;
import ar.edu.utn.dds.k3003.repositories.HeladeraMapper;
import ar.edu.utn.dds.k3003.repositories.HeladeraRepository;
import ar.edu.utn.dds.k3003.repositories.TemperaturaMapper;
import ar.edu.utn.dds.k3003.utils.DataDogsUtils;
import ar.edu.utn.dds.k3003.utils.MQUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.javalin.Javalin;
import io.javalin.micrometer.MicrometerPlugin;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class WebApp {
    static EntityManagerFactory entityManagerFactory;

    public static void main(String[] args) throws IOException, TimeoutException {
        HeladeraRepository heladeraRepository = new HeladeraRepository();
        HeladeraMapper heladeraMapper = new HeladeraMapper();
        TemperaturaMapper temperaturaMapper = new TemperaturaMapper();
        HeladeraJPARepository heladeraJPARepository = new HeladeraJPARepository();
        entityManagerFactory = startEntityManagerFactory();
        heladeraJPARepository.setEntityManagerFactory(entityManagerFactory);
        Map<String, String> env = System.getenv();
        MQUtils mqutils = new MQUtils(

                env.get("QUEUE_HOST"),
                env.get("QUEUE_USERNAME"),
                env.get("QUEUE_PASSWORD"),
                env.get("QUEUE_USERNAME"),
                env.get("QUEUE_NAME")
        );
        mqutils.init();
        Map<String, String> envMQ = System.getenv();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(envMQ.get("QUEUE_HOST"));
        factory.setUsername(envMQ.get("QUEUE_USERNAME"));
        factory.setPassword(envMQ.get("QUEUE_PASSWORD"));
// En el plan más barato, el VHOST == USER
        factory.setVirtualHost(envMQ.get("QUEUE_USERNAME"));
        String queueName = envMQ.get("QUEUE_NAME");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        TemperaturasWorker worker = new TemperaturasWorker(channel, queueName, entityManagerFactory);
        worker.init();

        MQUtils mqUtilsNotificaciones = new MQUtils(

                env.get("NOTIFICACIONES_HOST"),
                env.get("NOTIFICACIONES_USERNAME"),
                env.get("NOTIFICACIONES_PASSWORD"),
                env.get("NOTIFICACIONES_USERNAME"),
                env.get("NOTIFICACIONES_NAME")
        );
        mqUtilsNotificaciones.init();









        var DDUtils = new DataDogsUtils("Heladeras");
        var registro = DDUtils.getRegistro();

        // Metricas
        final var gauge = registro.gauge("ddsHeladeras.unGauge", new AtomicInteger(0));

        // Config
        final var micrometerPlugin = new MicrometerPlugin(config -> config.registry = registro);

        Integer port = Integer.parseInt(System.getProperty("PORT", "8080"));
        Javalin app = Javalin.create(config -> {
            config.registerPlugin(micrometerPlugin);
        }).start(port);
        app.get("/", ctx -> ctx.result("Hola Mundo"));

        // Start Cosas.


        // Si veo que en ningún controller uso directamente los repo o mapper (el mapper da igual), saco el constructor con todos los parámetros y lo dejo como new Fachada();
        Fachada fachadaHeladeras = new Fachada(heladeraJPARepository, heladeraMapper, temperaturaMapper, entityManagerFactory);
        var objectMapper = createObjectMapper();
        fachadaHeladeras.setViandasProxy(new ViandasProxy(objectMapper));


        // End Cosas.

        // Controllers.
        var agregarHeladeraController = new AgregarHeladeraController(fachadaHeladeras, registro);
        var obtenerHeladeraController = new ObtenerHeladeraController(fachadaHeladeras, entityManagerFactory, heladeraJPARepository);
        var depositarViandaController = new DepositarViandaController(fachadaHeladeras, registro);
        var listaHeladeraController = new ListaHeladeraController(fachadaHeladeras, heladeraMapper); // de test nomás. dsp borrarlo! -> y borrar Repo, Mapper, e iniciar solo Fachada() sin params.
        var retirarViandaController = new RetirarViandaController(fachadaHeladeras, registro, mqUtilsNotificaciones);
        var registrarTemperaturaController = new RegistrarTemperaturaController(fachadaHeladeras, mqutils);
        var obtenerTemperaturasController = new ObtenerTemperaturasController(fachadaHeladeras);
        var agregarSuscriptorController = new AgregarSuscriptorController(fachadaHeladeras);

        app.post("/heladeras", agregarHeladeraController::agregar);
        app.get("/heladeras/{id}", obtenerHeladeraController::obtenerHeladera);
        app.post("/depositos", depositarViandaController::depositarVianda);  // falta test en postman
        app.post("/retiros", retirarViandaController::retirarVianda);  // falta test en postman
        app.post("/temperaturas", registrarTemperaturaController::registrarTemperatura);
        app.get("/heladeras/{id}/temperaturas", obtenerTemperaturasController::obtenerTemperaturas);

        //agregarSuscriptor(Long colaborador_id, Integer heladera_id, int cantMinima, int viandasDisponibles, boolean notificarDesperfecto){
        app.post("/heladeras/suscripciones", agregarSuscriptorController::agregarSuscriptor);

        app.get("/listado", listaHeladeraController::listarHeladeras); // de test nomás. dsp borrarlo

    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
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
        String[] keys = new String[]{"javax.persistence.jdbc.url", "javax.persistence.jdbc.user",
                "javax.persistence.jdbc.password", "javax.persistence.jdbc.driver", "hibernate.hbm2ddl.auto",
                "hibernate.connection.pool_size", "hibernate.show_sql"};
        for (String key : keys) {
            if (env.containsKey(key)) {
                String value = env.get(key);
                configOverrides.put(key, value);
            }
        }
        return Persistence.createEntityManagerFactory("defaultdb", configOverrides);
    }

}
