package ar.edu.utn.dds.k3003.app;

import ar.edu.utn.dds.k3003.facades.dtos.TemperaturaDTO;
import ar.edu.utn.dds.k3003.model.Heladera;
import ar.edu.utn.dds.k3003.model.Temperatura;
import ar.edu.utn.dds.k3003.repositories.HeladeraJPARepository;
import ar.edu.utn.dds.k3003.repositories.TemperaturaMapper;
import com.rabbitmq.client.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class MovimientoWorker extends DefaultConsumer {

    private String queueName;
    private Fachada fachadaHeladeras;

    protected MovimientoWorker(Channel channel, String queueName, Fachada fachadaHeladeras) {
        super(channel);
        this.queueName = queueName;
        this.fachadaHeladeras = fachadaHeladeras;
    }

    public static void main(String[] args) throws Exception {
// Establecer la conexión con CloudAMQP
        Map<String, String> envMovimiento = System.getenv();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(envMovimiento.get("QUEUE_HOST"));
        factory.setUsername(envMovimiento.get("QUEUE_USERNAME"));
        factory.setPassword(envMovimiento.get("QUEUE_PASSWORD"));
// En el plan más barato, el VHOST == USER
        factory.setVirtualHost(envMovimiento.get("QUEUE_USERNAME"));
        String colaMovimiento = envMovimiento.get("COLA_MOVIMIENTO");
        Connection connection = factory.newConnection();
        Channel canalMovimiento = connection.createChannel();

        //TemperaturasWorker worker = new TemperaturasWorker(channel, queueName);
        //worker.init();
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
        String mensaje = new String(body, "UTF-8");
        mensaje = mensaje.substring(1,mensaje.length() - 1);
        Map<String,String> valores = new HashMap<>();
        String[] partes = mensaje.split(",");
        for (String parte:partes){
            String[] claveValor = parte.split("=");
            if(claveValor.length == 2){
                valores.put(claveValor[0],claveValor[1]);

            }
        }
        System.out.println("VALORES: " + valores);
        int heladeraId = Integer.parseInt(valores.get("heladera_id"));
        fachadaHeladeras.fraude(heladeraId);
        LocalDateTime fecha = LocalDateTime.now();
    }
}

