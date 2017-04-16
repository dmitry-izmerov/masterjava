package ru.javaops.masterjava.persist.model;

/**
 * Created by demi
 * on 15.04.17.
 */
public class Group extends BaseEntity {
	private String name;
	private GroupType type;

	public Group(Integer id, String name, GroupType type) {
		super(id);
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GroupType getType() {
		return type;
	}

	public void setType(GroupType type) {
		this.type = type;
	}
}
