package ru.javaops.masterjava.xml.repository;

import ru.javaops.masterjava.xml.schema.UserType;

import java.util.List;

/**
 * Created by demi
 * on 19.03.17.
 */
public interface ParticipantsRepository {
	List<UserType> getParticipantsByProject(String projectName) throws Exception;
}
