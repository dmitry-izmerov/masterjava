package ru.javaops.masterjava.service.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlMimeType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class Attachment {
	@XmlMimeType("application/octet-stream")
	private DataHandler dataHandler;
	private String name;
	private String contentType;

	public Attachment(DataHandler dataHandler) {
		this(dataHandler, null, null);
	}
}
