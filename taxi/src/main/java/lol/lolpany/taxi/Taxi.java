package lol.lolpany.taxi;

import com.rabbitmq.client.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;

class Taxi implements Runnable {

    private static final String QUEUE_PREFIX = "taxi_";

    private AtomicBoolean isOn;
    private final String host;
    private final String user;
    private final String password;
    private final String exchangeName;
    private final String directory;
    private final int number;

    Taxi(String host, String user, String password, String exchangeName, String directory, int number) {
        this.isOn = new AtomicBoolean(true);
        this.host = host;
        this.user = user;
        this.password = password;
        this.exchangeName = exchangeName;
        this.directory = directory;
        this.number = number;
    }

    @Override
    public void run() {
        try {
            File dir = new File(directory + File.separator + number);
            if (!dir.exists()) {
                Files.createDirectory(dir.toPath());
            }
            String queueName = QUEUE_PREFIX + number;
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setUsername(user);
            factory.setPassword(password);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
//            try (Connection connection = factory.newConnection();
//                 Channel channel = connection.createChannel()) {
                channel.queueDeclare(queueName, true, false, false, null);
                channel.queueBind(queueName, exchangeName, Integer.toString(number));
//                while (isOn.get()) {
                    channel.basicConsume(queueName, new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            FileUtils.writeByteArrayToFile(File.createTempFile("message", ".xml", dir), body);
                            try {
                                sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        }
                    });
//                    sleep(1000);
//                }
//            }
            while (isOn.get()) {
                sleep(1000);
            }
        } catch (TimeoutException | IOException | InterruptedException /*| InterruptedException*/ e) {
            e.printStackTrace();
        }
    }
}
