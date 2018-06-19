package ru.javaops.masterjava.upload;

import ru.javaops.masterjava.xml.schema.FlagType;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class UserUpload {
    public List<User> process(InputStream is) throws XMLStreamException {
        StaxStreamProcessor processor = new StaxStreamProcessor(is);
        List<User> users = new ArrayList<>();

        while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
            String email = processor.getAttribute("email");
            FlagType flag = FlagType.fromValue(processor.getAttribute("flag"));
            String fullName = processor.getReader().getElementText();
            User user = new User();
            user.setEmail(email);
            user.setValue(fullName);
            user.setFlag(flag);
            users.add(user);
        }
        return users;
    }
}
