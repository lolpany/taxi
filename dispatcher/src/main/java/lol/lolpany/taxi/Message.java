package lol.lolpany.taxi;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class Message {
    @XmlElement
    Dispatched dispatched;
    @XmlElement
    Target target;
    @XmlElement
    Sometags sometags;

    public static class Dispatched {
        @XmlAttribute
        long id;
    }

    public static class Target {
        @XmlAttribute
        String id;
    }

    public static class Sometags {
        @XmlElement
        List<Data> data;

        public static class Data {

        }
    }
}
