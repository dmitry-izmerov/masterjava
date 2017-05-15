package ru.javaops.masterjava.util;

import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

@AllArgsConstructor
@Data
public class FileInfo implements Serializable {
	private String fileName;
	private String contentType;
	private byte[] bytes;

	public static FileInfo of(String fileName, String contentType, InputStream inputStream) throws IOException {
		return new FileInfo(fileName, contentType, ByteStreams.toByteArray(inputStream));
	}
}
