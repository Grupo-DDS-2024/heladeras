package ar.edu.utn.dds.k3003.utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MQUtils {

    private String host;
    private String username;
    private String password;
    private String virtualHost;
    private String queueName;

    private Connection connection ;
    private Channel channel;

    public MQUtils(String host, String username, String password, String virtualHost, String queueName) {
        super();
        this.host = host;
        this.username = username;
        this.password = password;
        this.virtualHost = virtualHost;
        this.queueName = queueName;
    }

    public void init() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(this.host);
        factory.setUsername(this.username);
        factory.setPassword(this.password);
        factory.setVirtualHost(this.virtualHost);

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();

    }

    public void publish(String message) throws IOException {

        channel.basicPublish("", this.queueName, null, message.getBytes());
    }
}
