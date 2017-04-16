package ru.javaops.masterjava.persist.model;

import com.google.common.base.Objects;

/**
 * Created by demi
 * on 15.04.17.
 */
public class City extends BaseEntity {
	private String name;
	private String alias;

	// for jdbi's mapper
	public City() {}

	public City(String name, String alias) {
		this.name = name;
		this.alias = alias;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		City city = (City) o;
		return Objects.equal(alias, city.alias);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(alias);
	}
}
