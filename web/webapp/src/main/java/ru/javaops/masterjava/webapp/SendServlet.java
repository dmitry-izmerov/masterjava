package ru.javaops.masterjava.webapp;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.service.mail.Attachment;
import ru.javaops.masterjava.service.mail.MailWSClient;
import ru.javaops.web.WebStateException;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@WebServlet("/send")
@Slf4j
@MultipartConfig
public class SendServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String users = req.getParameter("users");
        String subject = req.getParameter("subject");
        String body = req.getParameter("body");
        String groupResult;
        try {
			List<Attachment> attachments = Lists.newArrayList();
			for (Part part : req.getParts()) {
				try (InputStream is = part.getInputStream()) {
					if (part.getName().startsWith("fileToUpload")) {
						ByteArrayDataSource dataSource = new ByteArrayDataSource(ByteStreams.toByteArray(is), null);
						attachments.add(new Attachment(new DataHandler(dataSource), part.getSubmittedFileName(), part.getContentType()));
					}
				}
			}
			groupResult = MailWSClient.sendBulk(MailWSClient.split(users), subject, body, attachments).toString();
		} catch (WebStateException e) {
            groupResult = e.toString();
        }
        resp.getWriter().write(groupResult);
    }
}
