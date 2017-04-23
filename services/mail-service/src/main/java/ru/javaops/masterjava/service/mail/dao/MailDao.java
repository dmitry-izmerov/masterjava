package ru.javaops.masterjava.service.mail.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.dao.AbstractDao;
import ru.javaops.masterjava.service.mail.model.MailingResult;

/**
 * Created by demi
 * on 20.04.17.
 */
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class MailDao implements AbstractDao {
	@SqlUpdate("TRUNCATE mailing_results")
	@Override
	public abstract void clean();

	@SqlUpdate("INSERT INTO mailing_results (value, \"to\", cc, subject) VALUES (CAST(:value AS MAILING_RESULT_TYPE), :to, :cc, :subject)")
	@GetGeneratedKeys
	public abstract int insertGeneratedId(@BindBean MailingResult mailingResult);

	public void insert(MailingResult mailingResult) {
		int id = insertGeneratedId(mailingResult);
		mailingResult.setId(id);
	}
}
