package ru.javaops.masterjava.service.mail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.web.WebStateException;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class MailServiceClient {

    public static void main(String[] args) throws MalformedURLException {
        Service service = Service.create(
                new URL(Configs.getConfig("hosts.conf", "hosts").getString("mail") + "/mail/mailService?wsdl"),
                new QName("http://mail.javaops.ru/", "MailServiceImplService"));

        MailService mailService = service.getPort(MailService.class);

        ImmutableSet<Addressee> addressees = ImmutableSet.of(
                new Addressee("gkislin@javaops.ru"),
                new Addressee("Мастер Java <masterjava@javaops.ru>"),
                new Addressee("Bad Email <bad_email.ru>")
		);

        try {
            String status = mailService.sendToGroup(addressees, ImmutableSet.of(), "Bulk email subject", "Bulk email body");
            System.out.println(status);

			File wsdlFile = Configs.getConfigFile("wsdl/mailService.wsdl");

			GroupResult groupResult = mailService.sendBulk(
            	addressees,
				"Individual mail subject",
				"Individual mail body",
				ImmutableList.of(new Attachment(new DataHandler(wsdlFile.toURI().toURL()), wsdlFile.getName(), "text/xml"))
			);
            System.out.println(groupResult);
        } catch (WebStateException e) {
            System.out.println(e);
        }
    }
}
