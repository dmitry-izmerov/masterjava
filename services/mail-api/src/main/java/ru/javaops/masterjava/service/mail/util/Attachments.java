package ru.javaops.masterjava.service.mail.util;

import com.google.common.io.ByteStreams;
import ru.javaops.masterjava.service.mail.Attach;
import ru.javaops.masterjava.util.FileInfo;

import javax.activation.DataHandler;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.util.ByteArrayDataSource;

public class Attachments {
    public static Attach getAttach(String name, String contentType, InputStream inputStream) throws IOException {
		ByteArrayDataSource dataSource = new ByteArrayDataSource(ByteStreams.toByteArray(inputStream), contentType);
        return new Attach(name, new DataHandler(dataSource));
    }

	public static Attach of(FileInfo fileInfo) {
		ByteArrayDataSource dataSource = new ByteArrayDataSource(fileInfo.getBytes(), fileInfo.getContentType());
		return new Attach(fileInfo.getFileName(), new DataHandler(dataSource));
	}
}
