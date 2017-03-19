package ru.javaops.masterjava.xml;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.util.XsltProcessor;

import java.io.InputStream;

/**
 * Created by demi
 * on 19.03.17.
 */
public class XstlExample {
	public static void main(String[] args) throws Exception {
		String projectName = "Spring Basics";

		try (InputStream xslInputStream = Resources.getResource("htmlTableWithGroups.xsl").openStream();
			 InputStream xmlInputStream = Resources.getResource("payload.xml").openStream()) {

			XsltProcessor processor = new XsltProcessor(xslInputStream);
			processor.setParameter("projectName", projectName);
			System.out.println(processor.transform(xmlInputStream));
		}
	}
}
