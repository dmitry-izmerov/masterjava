package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.Group;

import java.util.List;

/**
 * Created by demi
 * on 15.04.17.
 */
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class GroupDao implements AbstractDao {
	@SqlQuery("SELECT * FROM groups ORDER BY id ASC LIMIT :it")
	public abstract List<Group> getWithLimit(@Bind int limit);

	public Group insert(Group group) {
		if (group.isNew()) {
			int id = insertGeneratedId(group);
			group.setId(id);
		} else {
			insertWitId(group);
		}
		return group;
	}

	@SqlUpdate("INSERT INTO groups (name, type) VALUES (:name, CAST(:type as group_type))")
	@GetGeneratedKeys
	abstract int insertGeneratedId(@BindBean Group group);

	@SqlUpdate("INSERT INTO groups (id, name, type) VALUES (:id, :name, CAST(:type as group_type))")
	abstract void insertWitId(@BindBean Group group);

	@SqlBatch("INSERT INTO groups (name, type) VALUES (:name, CAST(:type as group_type)) ON CONFLICT DO NOTHING")
	public abstract int[] insertBatch(@BindBean List<Group> groups, @BatchChunkSize int chunkSize);

	@SqlUpdate("TRUNCATE groups")
	@Override
	public abstract void clean();
}
