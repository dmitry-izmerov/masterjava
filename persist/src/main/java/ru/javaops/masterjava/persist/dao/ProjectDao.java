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
import ru.javaops.masterjava.persist.model.Project;

import java.util.List;

/**
 * Created by demi
 * on 15.04.17.
 */
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class ProjectDao implements AbstractDao {

	@SqlQuery("SELECT * FROM projects ORDER BY id ASC LIMIT :it")
	public abstract List<Project> getWithLimit(@Bind int limit);

	public Project insert(Project project) {
		if (project.isNew()) {
			int id = insertGeneratedId(project);
			project.setId(id);
		} else {
			insertWitId(project);
		}
		return project;
	}

	@SqlUpdate("INSERT INTO projects (name, description) VALUES (:name, :description)")
	@GetGeneratedKeys
	abstract int insertGeneratedId(@BindBean Project project);

	@SqlUpdate("INSERT INTO projects (id, name, description) VALUES (:id, :name, :description)")
	abstract void insertWitId(@BindBean Project project);

	@SqlBatch("INSERT INTO projects (name, description) VALUES (:name, :description) ON CONFLICT DO NOTHING")
	public abstract int[] insertBatch(@BindBean List<Project> projects, @BatchChunkSize int chunkSize);

	@SqlUpdate("TRUNCATE projects")
	@Override
	public abstract void clean();
}
