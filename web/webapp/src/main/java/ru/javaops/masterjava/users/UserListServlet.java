package ru.javaops.masterjava.users;

import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.persist.DBIProvider;
import ru.javaops.masterjava.persist.dao.UserDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static ru.javaops.masterjava.common.web.ThymeleafListener.engine;

@WebServlet("/")
public class UserListServlet extends HttpServlet {

	private final UserDao userDao = DBIProvider.getDao(UserDao.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());
		webContext.setVariable("users", userDao.getAllByOrderWithLimit("id", "ASC", 20));
		engine.process("users", webContext, resp.getWriter());
	}
}
