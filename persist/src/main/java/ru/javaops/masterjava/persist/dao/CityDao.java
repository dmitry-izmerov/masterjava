package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.City;

import java.util.List;

/**
 * Created by demi
 * on 15.04.17.
 */
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class CityDao implements AbstractDao {
	private static final int DEFAULT_CHUNK_SIZE = 100;

	@SqlQuery("SELECT * FROM cities ORDER BY id ASC")
	public abstract List<City> getAll();

	@SqlQuery("SELECT * FROM cities WHERE alias = :alias")
	public abstract City getByAlias(@Bind("alias") String alias);

	@SqlBatch("INSERT INTO cities (name, alias) VALUES (:name, :alias) ON CONFLICT DO NOTHING")
	@BatchChunkSize(DEFAULT_CHUNK_SIZE)
	public abstract int[] insertBatch(@BindBean List<City> cities);

	@SqlUpdate("TRUNCATE cities CASCADE")
	@Override
	public abstract void clean();
}
