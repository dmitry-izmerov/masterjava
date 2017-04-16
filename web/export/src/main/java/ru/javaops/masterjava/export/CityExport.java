package ru.javaops.masterjava.export;

import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.CityDao;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by demi
 * on 16.04.17.
 */
public class CityExport {
	private final CityDao cityDao = DBIProvider.getDao(CityDao.class);

	public void process(final StaxStreamProcessor processor) throws XMLStreamException {
		List<City> cities = new ArrayList<>();
		while (processor.doUntil(XMLEvent.START_ELEMENT, "City", XMLEvent.END_ELEMENT, "Cities")) {
			final String alias = processor.getAttribute("id");
			final String name = processor.getText();
			final City city = new City(name, alias);
			cities.add(city);
		}
		cityDao.insertBatch(cities);
	}
}
