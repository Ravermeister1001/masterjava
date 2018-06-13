package ru.javaops.masterjava;

import com.google.common.io.Resources;
import j2html.tags.ContainerTag;
import ru.javaops.masterjava.xml.schema.*;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.sql.rowset.spi.XmlReader;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static j2html.TagCreator.*;

public class MainXml {
    static URL payloadUrl;
    public static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getValue).thenComparing(User::getEmail);
    public static void main(String[] args) throws IOException, JAXBException, XMLStreamException {
        payloadUrl = Resources.getResource("payload.xml");
        MainXml main = new MainXml();
        String projectName = args[0];

        //Set<User> users = getUsersByJaxb(projectName);
        //String htmlData = getHtmlData(users, projectName, Paths.get("out/usersJaxb.html"));
        Set<User> users = getUsersByStax(projectName);
        String htmlData = getHtmlData(users, projectName, Paths.get("out/usersStax.html"));
        System.out.println(htmlData);
    }

    private static Set<User> getUsersByStax(String projectName) throws IOException, XMLStreamException {
        try (InputStream is = payloadUrl.openStream()) {
            StaxStreamProcessor processor = new StaxStreamProcessor(is);
            Set<String> groups = new HashSet<>();
            Set<User> users = new TreeSet<>(USER_COMPARATOR);
            XMLStreamReader reader = processor.getReader();

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if ("Project".equals(reader.getLocalName()) && projectName.equals(reader.getAttributeValue(0))) {
                        while (reader.hasNext()) {
                            event = reader.next();
                            if ( event == XMLEvent.START_ELEMENT && "Group".equals(reader.getLocalName())) {
                                groups.add(reader.getAttributeValue(0));
                            }
                            if (event == XMLEvent.END_ELEMENT && "Project".equals(reader.getLocalName())) {
                                break;
                            }
                        }
                    }

                    if ("User".equals(reader.getLocalName())) {
                        String userEmail = reader.getAttributeValue(2);
                        String[] userGroups = reader.getAttributeValue(3).split(" ");
                        String userName = reader.getElementText();

                        if (groups.stream()
                                .filter(gr -> Arrays.stream(userGroups)
                                        .filter(ug -> groups.contains(ug)).findAny().isPresent())
                                .findAny().isPresent()) {
                            User userToAdd = new User();
                            userToAdd.setEmail(userEmail);
                            userToAdd.setValue(userName);
                            users.add(userToAdd);
                        }
                    }
                }
            }
            return users;
        }
    }


    private static Set<User> getUsersByJaxb(String projectName) throws IOException, JAXBException {
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

    private static String getHtmlData(Set<User> users, String projectName, Path path) throws IOException {
        try (Writer writer = Files.newBufferedWriter(path)) {
            final ContainerTag table = table().with(tr().with(th("FullName"), th("email")));
            users.forEach(u -> table.with(tr().with(td(u.getValue()), td(u.getEmail()))));
            table.attr("border", "1");

            String out = html().with(head().with(title(projectName + "users")),
                    body().with(h1(projectName + "users")), table).render();

            writer.write(out);
            return out;
        }
    }
}
