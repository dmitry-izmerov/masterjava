package ru.javaops.masterjava.xml;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.UserType;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by demi
 * on 18.03.17.
 */
public class MainXml {
	private static final String PAYLOAD_XML = "payload.xml";
	private static final String PAYLOAD_XSD = "payload.xsd";

	public static void main(String[] args) throws Exception {
		String projectName = "Spring Basics";
		printParticipantsByProject(projectName);
	}

	private static void printParticipantsByProject(String projectName) throws IOException, JAXBException {
		InputStream inputStream = Resources.getResource(PAYLOAD_XML).openStream();
		JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
		jaxbParser.setSchema(Schemas.ofClasspath(PAYLOAD_XSD));
		Payload payload = jaxbParser.unmarshal(inputStream);
		List<UserType> users = payload.getProjects()
				.getProject()
				.stream()
				.filter(project -> project.getName().equals(projectName))
				.map(project -> project.getGroup())
				.flatMap(groups -> groups.stream())
				.map(group -> group.getParticipant())
				.flatMap(participants -> participants.stream())
				.map(participant -> ((UserType) participant.getUser()))
				.filter(distinctByKey(user -> user.getFullName()))
				.sorted(Comparator.comparing(UserType::getFullName))
				.collect(Collectors.toList());

		for (UserType user : users) {
			System.out.println(user.getFullName());
		}
	}

	private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
		Map<Object,Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
