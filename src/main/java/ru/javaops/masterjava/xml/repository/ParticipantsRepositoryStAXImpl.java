package ru.javaops.masterjava.xml.repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.FlagType;
import ru.javaops.masterjava.xml.schema.UserType;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by demi
 * on 19.03.17.
 */
public class ParticipantsRepositoryStAXImpl implements ParticipantsRepository {

	private String xmlFileName;
	private Map<String, UserType> users = new HashMap<>();

	public ParticipantsRepositoryStAXImpl(String xmlFileName) {
		this.xmlFileName = xmlFileName;
	}

	@Override
	public List<UserType> getParticipantsByProject(String projectName) throws Exception {
		List<UserType> participants = Lists.newArrayList();
		try (StaxStreamProcessor processor = new StaxStreamProcessor(Resources.getResource(xmlFileName).openStream())) {
			XMLStreamReader reader = processor.getReader();
			while (reader.hasNext()) {
				int event = reader.next();
				if (event == XMLEvent.START_ELEMENT) {
					String attrValue;

					if ("Users".equals(reader.getLocalName())) {
						users = getUsers(reader);
					} else if ("Project".equals(reader.getLocalName()) && (attrValue = reader.getAttributeValue(null, "name")) != null && attrValue.equals(projectName)) {
						participants = getParticipants(reader);
						break;
					}
				}
			}
		}

		return participants
				.stream()
				.sorted(Comparator.comparing(UserType::getFullName))
				.collect(Collectors.toList());
	}

	private Map<String, UserType> getUsers(XMLStreamReader reader) throws XMLStreamException {
		Map<String, UserType> users = Maps.newHashMap();
		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLEvent.START_ELEMENT) {
				if ("User".equals(reader.getLocalName())) {
					String id = reader.getAttributeValue(null, "id");
					users.put(id, getUser(reader));
				}
			}

			if (event == XMLEvent.END_ELEMENT && "Users".equals(reader.getLocalName())) {
				break;
			}
		}
		return users;
	}

	private UserType getUser(XMLStreamReader reader) throws XMLStreamException {
		UserType user = new UserType();
		user.setId(reader.getAttributeValue(null, "id"));
		user.setEmail(reader.getAttributeValue(null, "email"));
		user.setFlag(FlagType.fromValue(reader.getAttributeValue(null, "flag")));
		user.setCity(reader.getAttributeValue(null, "city"));

		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLEvent.START_ELEMENT) {
				if ("fullName".equals(reader.getLocalName())) {
					user.setFullName(reader.getElementText());
				}
			}

			if (event == XMLEvent.END_ELEMENT) {
				break;
			}
		}
		return user;
	}

	private List<UserType> getParticipants(XMLStreamReader reader) throws XMLStreamException {
		Set<UserType> participants = Sets.newHashSet();
		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLEvent.START_ELEMENT) {
				if ("Group".equals(reader.getLocalName())) {
					participants.addAll(getParticipantsFromGroup(reader));
				}
			}

			if (event == XMLEvent.END_ELEMENT && "Project".equals(reader.getLocalName())) {
				break;
			}
		}
		return Lists.newArrayList(participants);
	}

	private List<? extends UserType> getParticipantsFromGroup(XMLStreamReader reader) throws XMLStreamException {
		List<UserType> participants = Lists.newArrayList();
		while (reader.hasNext()) {
			int event = reader.next();
			if (event == XMLEvent.START_ELEMENT) {
				if ("Participant".equals(reader.getLocalName())) {
					getParticipant(reader).ifPresent(participants::add);
				}
			}

			if (event == XMLEvent.END_ELEMENT && "Group".equals(reader.getLocalName())) {
				break;
			}
		}
		return participants;
	}

	private Optional<UserType> getParticipant(XMLStreamReader reader) throws XMLStreamException {
		return Optional.ofNullable(users.get(reader.getAttributeValue(null, "user")));
	}
}
