package ru.javaops.masterjava.service.mail.rest;


import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.hibernate.validator.constraints.NotBlank;
import ru.javaops.masterjava.service.mail.Attach;
import ru.javaops.masterjava.service.mail.GroupResult;
import ru.javaops.masterjava.service.mail.MailServiceExecutor;
import ru.javaops.masterjava.service.mail.MailWSClient;
import ru.javaops.masterjava.service.mail.util.Attachments;
import ru.javaops.web.WebStateException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Path("/")
public class MailRS {
    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "Test";
    }

    @POST
    @Path("send")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
    public GroupResult send(
		@NotBlank @FormDataParam("users") String users,
		@FormDataParam("subject") String subject,
		@NotBlank @FormDataParam("body") String body,
		@FormDataParam("attach") InputStream fileStream,
		@FormDataParam("attach")FormDataBodyPart formDataBodyPart
	) throws WebStateException {
		List<Attach> attaches;
		if (fileStream == null || formDataBodyPart == null) {
			attaches = ImmutableList.of();
		} else {
			try {
				attaches = ImmutableList.of(Attachments.getAttach(formDataBodyPart.getContentDisposition().getFileName(), formDataBodyPart.getMediaType().toString(), fileStream));
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				throw new WebStateException(e);
			}
		}

        return MailServiceExecutor.sendBulk(MailWSClient.split(users), subject, body, attaches);
	}
}