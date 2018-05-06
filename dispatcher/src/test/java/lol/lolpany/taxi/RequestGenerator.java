package lol.lolpany.taxi;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;

import static com.jayway.restassured.RestAssured.given;

public class RequestGenerator implements Callable<Void> {

    private final long numberOfRequest;

    public RequestGenerator(long numberOfRequest) {
        this.numberOfRequest = numberOfRequest;
    }

    @Override
    public Void call() {
        try {
            Marshaller marshaller = JAXBContext.newInstance(Message.class).createMarshaller();
            for (int i = 0; i < numberOfRequest; i++) {
                Message message = new Message();
                message.target = new Message.Target();
                message.target.id = Integer.toString(i % 10);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                marshaller.marshal(message, byteArrayOutputStream);
                given().request().contentType("application/xml").body(byteArrayOutputStream.toString())
                        .when().post("http://localhost:8080/message");
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }
}
