package ru.javaops.masterjava.controller;

import ru.javaops.masterjava.xml.schema.FlagType;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

@WebServlet(name = "FileUploadController", urlPatterns = "/upload")
@MultipartConfig(location = "/tmp")
public class FileUploadController extends HttpServlet {

	private static final Comparator<User> USER_COMPARATOR = Comparator.comparing(User::getValue).thenComparing(User::getEmail);
	private static final String UPLOAD_DIR = "/tmp";
	private static final String FILE_UPLOAD_JSP = "/WEB-INF/classes/views/file-upload.jsp";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		getServletContext().getRequestDispatcher(FILE_UPLOAD_JSP).forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String filePath = null;
		//Get all the parts from request and write it to the file on server
		for (Part part : request.getParts()) {
			if (filePath == null) {
				String fileName = getFileName(part);
				if (fileName.isEmpty()) {
					break;
				}

				filePath = UPLOAD_DIR + File.separator + fileName;
			}
			part.write(filePath);
		}

		request.setAttribute("users", null);
		if (filePath != null && !filePath.isEmpty()) {
			try {
				request.setAttribute("users", getUsers(Paths.get(filePath).toUri().toURL()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		getServletContext().getRequestDispatcher(FILE_UPLOAD_JSP).forward(request, response);
	}

	/**
	 * Utility method to get file name from HTTP header content-disposition
	 */
	private String getFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] tokens = contentDisp.split(";");
		for (String token : tokens) {
			if (token.trim().startsWith("filename")) {
				return token.substring(token.indexOf("=") + 2, token.length()-1);
			}
		}
		return "";
	}

	private static Set<User> getUsers(URL payloadUrl) throws Exception {

		try (InputStream is = payloadUrl.openStream()) {
			StaxStreamProcessor processor = new StaxStreamProcessor(is);

			// Users loop
			Set<User> users = new TreeSet<>(USER_COMPARATOR);

			while (processor.doUntil(XMLEvent.START_ELEMENT, "User")) {
				User user = new User();
				user.setEmail(processor.getAttribute("email"));
				user.setFlag(FlagType.fromValue(processor.getAttribute("flag")));
				user.setValue(processor.getText());
				users.add(user);
			}
			return users;
		}
	}
}