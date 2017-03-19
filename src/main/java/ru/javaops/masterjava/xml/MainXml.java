package ru.javaops.masterjava.xml;

import ru.javaops.masterjava.xml.repository.ParticipantsRepository;
import ru.javaops.masterjava.xml.repository.ParticipantsRepositoryJAXBImpl;
import ru.javaops.masterjava.xml.repository.ParticipantsRepositoryStAXImpl;
import ru.javaops.masterjava.xml.schema.UserType;
import java.util.Comparator;
import java.util.List;

/**
 * Created by demi
 * on 18.03.17.
 */
public class MainXml {
	private static final String PAYLOAD_XML = "payload.xml";
	private static final String PAYLOAD_XSD = "payload.xsd";

	private static ParticipantsRepository participantsRepositoryStAX = new ParticipantsRepositoryStAXImpl(PAYLOAD_XML);
	private static ParticipantsRepository participantsRepositoryJAXB = new ParticipantsRepositoryJAXBImpl(PAYLOAD_XML, PAYLOAD_XSD);

	public static void main(String[] args) throws Exception {
		String projectName = "Spring Basics";
		printParticipants(projectName);
	}

	private static void printParticipants(String projectName) throws Exception {
		List<UserType> participants = participantsRepositoryStAX.getParticipantsByProject(projectName);
//		List<UserType> participants = participantsRepositoryJAXB.getParticipantsByProject(projectName);
		participants
			.stream()
			.sorted(Comparator.comparing(UserType::getFullName))
			.forEach(user -> System.out.printf("name = %s, email = %s%n", user.getFullName(), user.getEmail()));
	}
}
