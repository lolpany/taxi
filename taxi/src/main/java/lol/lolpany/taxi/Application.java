package lol.lolpany.taxi;

import java.io.IOException;
import java.util.Properties;

public class Application {

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(Application.class.getResourceAsStream("../../../application.properties"));
        for (int i = 0; i < 10; i++) {
            new Thread(new Taxi(properties.getProperty("rabbitmq.host"), properties.getProperty("rabbitmq.username"),
                    properties.getProperty("rabbitmq.password"), properties.getProperty("rabbitmq.exchange.name"),
                    properties.getProperty("taxi.directory"), i)).start();
        }
    }
}
