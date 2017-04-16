package ru.javaops.masterjava.persist.model;

import java.util.Set;

/**
 * Created by demi
 * on 15.04.17.
 */
public class Project extends BaseEntity {
	private String name;
	private String description;
	private Set<Group> groups;

	public Project(Integer id, String name, String description, Set<Group> groups) {
		super(id);
		this.name = name;
		this.description = description;
		this.groups = groups;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}
}
