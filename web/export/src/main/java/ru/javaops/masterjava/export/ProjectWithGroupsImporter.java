package ru.javaops.masterjava.export;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.GroupDao;
import ru.javaops.masterjava.persist.dao.ProjectDao;
import ru.javaops.masterjava.persist.model.Group;
import ru.javaops.masterjava.persist.model.GroupType;
import ru.javaops.masterjava.persist.model.Project;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by demi
 * on 22.04.17.
 */
@Slf4j
public class ProjectWithGroupsImporter {
	private final ProjectDao projectDao = DBIProvider.getDao(ProjectDao.class);
	private final GroupDao groupDao = DBIProvider.getDao(GroupDao.class);

	public Map<String, Group> process(StaxStreamProcessor processor) throws XMLStreamException {
		val projectMap = projectDao.getAsMap();
		val newProjects = new ArrayList<Project>();
		val groupMap = groupDao.getAsMap();
		val newGroups = new ArrayList<Group>();
		int id = projectDao.getCurrentId();

		Project project = null;
		String element;
		while ((element = processor.doUntilAny(XMLEvent.START_ELEMENT, "Project", "Group", "Cities")) != null) {
			if ("Cities".equals(element)) break;

			if ("Project".equals(element)) {
				val name = processor.getAttribute("name");
				if (!projectMap.containsKey(name)) {
					project = new Project(++id, name, processor.getElementValue("description"));
					newProjects.add(project);
				}
			} else if ("Group".equals(element)) {
				val name = processor.getAttribute("name");
				if (!groupMap.containsKey(name)) {
					newGroups.add(new Group(++id, name, GroupType.valueOf(processor.getAttribute("type")), project.getId()));
				}
			}
		}
		log.info("Insert batch " + newProjects);
		projectDao.insertBatch(newProjects);

		log.info("Insert batch " + newGroups);
		groupDao.insertBatch(newGroups);

		return groupDao.getAsMap();
	}
}
