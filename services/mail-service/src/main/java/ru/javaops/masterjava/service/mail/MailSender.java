package ru.javaops.masterjava.service.mail;

import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import ru.javaops.masterjava.config.Configs;

import java.util.List;

/**
 * gkislin
 * 15.11.2016
 */
@Slf4j
public class MailSender {

	private static final Config config = Configs.getConfig("mail.conf", "mail");

    static void sendMail(List<Addressee> to, List<Addressee> cc, String subject, String body) throws EmailException {
        log.info("Send mail to \'" + to + "\' cc \'" + cc + "\' subject \'" + subject + (log.isDebugEnabled()?"\nbody=" + body:""));
		Email email = new SimpleEmail();
		email.setHostName(config.getString("host"));
		email.setSmtpPort(config.getInt("port"));
		email.setAuthenticator(new DefaultAuthenticator(config.getString("username"), config.getString("password")));
		email.setSSLOnConnect(config.getBoolean("useSSL"));
		email.setFrom(config.getString("username"), config.getString("fromName"));
		email.setSubject(subject);
		email.setMsg(body);
		for (Addressee addressee: to) {
			email.addTo(addressee.getEmail(), addressee.getName());
		}
		for (Addressee addressee: cc) {
			email.addTo(addressee.getEmail(), addressee.getName());
		}
		email.send();
    }
}
