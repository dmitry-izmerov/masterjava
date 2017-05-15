package ru.javaops.masterjava.service.mail.jms;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import ru.javaops.masterjava.service.mail.Attach;
import ru.javaops.masterjava.service.mail.GroupResult;
import ru.javaops.masterjava.service.mail.MailWSClient;
import ru.javaops.masterjava.service.mail.util.Attachments;
import ru.javaops.masterjava.util.FileInfo;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.List;

@WebListener
@Slf4j
public class JmsListener implements ServletContextListener {
    private Thread listenerThread = null;
    private QueueConnection connection;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            InitialContext initCtx = new InitialContext();
			ActiveMQConnectionFactory connectionFactory = (ActiveMQConnectionFactory) initCtx.lookup("java:comp/env/jms/ConnectionFactory");
			connectionFactory.setTrustAllPackages(true);
            connection = connectionFactory.createQueueConnection();
            QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = (Queue) initCtx.lookup("java:comp/env/jms/queue/MailQueue");
            QueueReceiver receiver = queueSession.createReceiver(queue);
            connection.start();
            log.info("Listen JMS messages ...");
            listenerThread = new Thread(() -> {
				while (!Thread.interrupted()) {
					try {
						Message m = receiver.receive();
						if (m instanceof ObjectMessage) {
							ObjectMessage objectMessage = (ObjectMessage) m;
							String users = objectMessage.getStringProperty("users");
							String subject = objectMessage.getStringProperty("subject");
							String body = objectMessage.getStringProperty("body");
							List<Attach> attaches = Lists.newArrayList();
							List<FileInfo> files = (List<FileInfo>) objectMessage.getObject();
							for (FileInfo fileInfo : files) {
								attaches.add(Attachments.of(fileInfo));
							}
							GroupResult groupResult = MailWSClient.sendBulk(MailWSClient.split(users), subject, body, attaches);
							log.info("Result of mailing: {}.", groupResult);
						}
					} catch (Exception e) {
						log.error("Receiving messages failed: " + e.getMessage(), e);
					}
				}
            });
            listenerThread.start();
        } catch (Exception e) {
            log.error("JMS failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException ex) {
                log.warn("Couldn't close JMSConnection: ", ex);
            }
        }
        if (listenerThread != null) {
            listenerThread.interrupt();
        }
    }
}