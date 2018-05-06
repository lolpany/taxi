package lol.lolpany.taxi;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class Dispatcher {

    JAXBContext jaxbContext;

    AtomicLong dispatchedCounter;

    @Value("${counter.file.location}")
    private String counterFile;
    @Value("${rabbitmq.host}")
    private String host;
    @Value("${rabbitmq.username}")
    private String user;
    @Value("${rabbitmq.password}")
    private String password;
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    public Dispatcher() throws JAXBException {
        this.jaxbContext = JAXBContext.newInstance(Message.class);
    }

    @PostConstruct
    public void postConstruct() throws IOException {
        this.dispatchedCounter = new AtomicLong(Long.parseLong(FileUtils.readFileToString(new File(counterFile))));
    }

    @RequestMapping(value = "/message", method = RequestMethod.POST, consumes = "application/xml;charset=UTF-8")
    public void message(@RequestBody Message message) throws IOException, TimeoutException, JAXBException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(user);
        factory.setPassword(password);
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            Marshaller marshaller = jaxbContext.createMarshaller();
            Message.Dispatched dispatched = new Message.Dispatched();
            dispatched.id = dispatchedCounter.incrementAndGet();
            message.dispatched = dispatched;
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            marshaller.marshal(message, byteOutputStream);
            channel.basicPublish(exchangeName, message.target.id, true, false, new AMQP.BasicProperties().builder().deliveryMode(JmsProperties.DeliveryMode.PERSISTENT.getValue()).build(), byteOutputStream.toByteArray());
            channel.addReturnListener((replyCode, replyText, exchange, routingKey, properties, body) -> {
                throw new IOException("Processing failed!");
            });
        }
    }

    @PreDestroy
    public void preDestroy() throws IOException {
        FileUtils.writeStringToFile(new File(counterFile), dispatchedCounter.toString(), StandardCharsets.UTF_8.toString());
    }


}
