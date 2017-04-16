package ru.javaops.masterjava.export;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;

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

import static ru.javaops.masterjava.common.web.ThymeleafListener.engine;

@WebServlet("/")
@MultipartConfig
@Slf4j
public class UploadServlet extends HttpServlet {
    private static final int CHUNK_SIZE = 2000;
	private final UserExport userExport = new UserExport();
	private final CityExport cityExport = new CityExport();

	@Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        outExport(req, resp, "", CHUNK_SIZE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message;
        int chunkSize = CHUNK_SIZE;
        try {
//            http://docs.oracle.com/javaee/6/tutorial/doc/glraq.html
            chunkSize = Integer.parseInt(req.getParameter("chunkSize"));
            if (chunkSize < 1) {
                message = "Chunk Size must be > 1";
            } else {
                Part filePart = req.getPart("fileToUpload");

				try (final StaxStreamProcessor processor = new StaxStreamProcessor(filePart.getInputStream())) {
					cityExport.process(processor);
					List<UserExport.FailedEmail> failed = userExport.process(processor, chunkSize);
                    log.info("Failed users: " + failed);
                    final WebContext webContext =
                            new WebContext(req, resp, req.getServletContext(), req.getLocale(),
                                    ImmutableMap.of("failed", failed));
                    engine.process("result", webContext, resp.getWriter());
                    return;
                }
            }
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            message = e.toString();
        }
        outExport(req, resp, message, chunkSize);
    }

    private void outExport(HttpServletRequest req, HttpServletResponse resp, String message, int chunkSize) throws IOException {
        resp.setCharacterEncoding("utf-8");
        final WebContext webContext =
                new WebContext(req, resp, req.getServletContext(), req.getLocale(),
                        ImmutableMap.of("message", message, "chunkSize", chunkSize));
        engine.process("export", webContext, resp.getWriter());
    }
}
