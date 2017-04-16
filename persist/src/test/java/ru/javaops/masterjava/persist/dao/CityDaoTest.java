package ru.javaops.masterjava.persist.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.javaops.masterjava.persist.model.City;

import java.util.Arrays;
import java.util.List;

/**
 * Created by demi
 * on 16.04.17.
 */
public class CityDaoTest extends AbstractDaoTest<CityDao> {

	private static final List<City> CITIES = Arrays.asList(new City("moscow", "msk"), new City("sergiev posad", "spd"));

	public CityDaoTest() {
		super(CityDao.class);
	}

	@Before
	public void setUp() throws Exception {
		dao.clean();
	}

	@After
	public void cleanUp() {
		dao.clean();
	}

	@Test
	public void getByAlias() throws Exception {
		dao.insertBatch(CITIES);
		Assert.assertEquals(CITIES.get(0), dao.getByAlias("msk"));
	}

	@Test
	public void insertBatch() throws Exception {
		dao.insertBatch(CITIES);
		Assert.assertEquals(CITIES, dao.getAll());
	}
}