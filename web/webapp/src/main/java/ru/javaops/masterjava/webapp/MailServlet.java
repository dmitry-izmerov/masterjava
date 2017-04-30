package ru.javaops.masterjava.webapp;

import com.google.common.collect.Maps;
import one.util.streamex.StreamEx;
import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;
import ru.javaops.masterjava.persist.model.UserFlag;
import ru.javaops.masterjava.service.mail.Addressee;
import ru.javaops.masterjava.service.mail.GroupResult;
import ru.javaops.masterjava.service.mail.MailService;
import ru.javaops.masterjava.service.mail.MailServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

import static ru.javaops.masterjava.common.web.ThymeleafListener.engine;

/**
 * Created by demi
 * on 29.04.17.
 */
@WebServlet("/mailing")
public class MailServlet extends HttpServlet {
	private UserDao userDao = DBIProvider.getDao(UserDao.class);
	private MailService mailService = new MailServiceImpl();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HashMap<String, Object> variables = Maps.newHashMap();
		variables.put("users", userDao.getWithLimitByFlag(UserFlag.active, 20));
		variables.put("message", req.getSession().getAttribute("message"));
		req.getSession().setAttribute("message", null);

		final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale(), variables);
		engine.process("mailing", webContext, resp.getWriter());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String message;
		String[] emails = req.getParameterValues("email");
		String subject = req.getParameter("subject");
		String body = req.getParameter("body");
		if (emails == null || emails.length == 0) {
			message = "You didn't select any emails for mailing.";
		} else if (subject == null || subject.length() == 0) {
			message = "You must set subject of mail.";
		} else if (body == null || body.length() == 0) {
			message = "You must set body of mail.";
		} else {
			GroupResult groupResult = mailService.sendBulk(StreamEx.of(emails).map(Addressee::new).toSet(), subject, body);
			message = groupResult.toString();
		}
		req.getSession().setAttribute("message", message);
		resp.sendRedirect("/mailing");
	}
}
