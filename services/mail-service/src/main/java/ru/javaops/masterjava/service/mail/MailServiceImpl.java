package ru.javaops.masterjava.service.mail;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.EmailException;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.service.mail.dao.MailDao;
import ru.javaops.masterjava.service.mail.model.MailingResult;
import ru.javaops.masterjava.service.mail.model.MailingResultType;

import javax.jws.WebService;
import java.util.List;

/**
 * gkislin
 * 15.11.2016
 */
@Slf4j
@WebService(endpointInterface = "ru.javaops.masterjava.service.mail.MailService")
public class MailServiceImpl implements MailService {

	private MailDao mailDao = DBIProvider.getDao(MailDao.class);

    public void sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) {
		try {
			MailSender.sendMail(to, cc, subject, body);
			mailDao.insert(new MailingResult(MailingResultType.SUCCESS, to.toString(), cc.toString(), subject));
		} catch (EmailException e) {
			log.error("Sending mail is failed to {}", to, e);
			mailDao.insert(new MailingResult(MailingResultType.FAILED, to.toString(), cc.toString(), subject));
		}
	}
}