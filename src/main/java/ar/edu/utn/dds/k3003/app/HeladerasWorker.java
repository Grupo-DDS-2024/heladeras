package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.model.Temperatura;
import ar.edu.utn.dds.k3003.repositories.HeladeraJPARepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

public class HeladerasWorker extends DefaultConsumer {

    private String queueName;
    private EntityManagerFactory entityManagerFactory;

    protected HeladerasWorker(Channel channel, String queueName, EntityManagerFactory entityManagerFactory) {
        super(channel);
        this.queueName = queueName;
        this.entityManagerFactory = entityManagerFactory;
    }

    public static void main(String[] args) throws Exception {
// Establecer la conexión con CloudAMQP
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

        EntityManagerFactory entityManagerFactory2 = WebApp.startEntityManagerFactory();

        HeladerasWorker worker = new HeladerasWorker(channel, queueName, entityManagerFactory2);
        worker.init();
    }

    public void init() throws IOException {
// Declarar la cola desde la cual consumir mensajes
        this.getChannel().queueDeclare(this.queueName, false, false, false, null);
// Consumir mensajes de la cola
        this.getChannel().basicConsume(this.queueName, false, this);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
                               AMQP.BasicProperties properties, byte[] body) throws IOException {
// Confirmar la recepción del mensaje a la mensajeria
        this.getChannel().basicAck(envelope.getDeliveryTag(), false);
        String analisisId = new String(body, "UTF-8");
        String temp = analisisId.substring(analisisId.indexOf("TemperaturaDTO(") + "TemperaturaDTO(".length(), analisisId.length() - 2);
        String[] campos = temp.split(", ");
        int tempe = 0;
        int heladeriaId = 400;
        LocalDateTime fecha = null;
        for (String campo : campos) {
            if (campo.startsWith("temperatura=")) {
                tempe = Integer.parseInt(campo.split("=")[1]);
            }
            if (campo.startsWith("heladeraId=")) {
                heladeriaId = Integer.parseInt(campo.split("=")[1]);
            }
            if (campo.startsWith("fechaMedicion=")) {
                fecha = LocalDateTime.parse(campo.split("=")[1]);
            }
        }
        System.out.println("id");
        System.out.println(heladeriaId);
        Temperatura temperatura = new Temperatura(tempe, heladeriaId, fecha);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        HeladeraJPARepository repositorio = new HeladeraJPARepository(entityManager);
        Heladera heladera = repositorio.findById(heladeriaId);
        heladera.setTemperatura(temperatura);
        repositorio.save(heladera); //dsp cambiarlo a update
        entityManager.getTransaction().commit();
        entityManager.close();

    }


}

