package ru.javaops.masterjava;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.*;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class MainXml {
    static URL payloadUrl;
    public static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getValue).thenComparing(User::getEmail);
    public static void main(String[] args) throws IOException, JAXBException {
        payloadUrl = Resources.getResource("payload.xml");
        MainXml main = new MainXml();
        String projectName = args[0];

        Set<User> users = getUsers(projectName);
        for (User user : users) {
            System.out.println(user.getValue());
        }
    }

    private static Set<User> getUsers(String projectName) throws IOException, JAXBException {
        JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
        jaxbParser.setSchema(Schemas.ofClasspath("payload.xsd"));
        try (InputStream is = payloadUrl.openStream()) {
            Payload payload = jaxbParser.unmarshal(is);
            Project project = payload.getProjects().getProject().stream()
                    .filter(p -> projectName.equals(p.getName()))
                    .findFirst()
                    .get();

            Set<Group> groups = new HashSet<>(project.getGroup());
            return payload.getUsers().getUser().stream()
                    .filter(u -> u.getGroups().stream().filter(gr -> groups.contains(gr)).findAny()
                    .isPresent())
                    .collect(Collectors.toCollection(() -> new TreeSet<>(USER_COMPARATOR)));
        }
    }
}
