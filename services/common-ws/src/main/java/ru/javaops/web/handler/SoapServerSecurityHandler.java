package ru.javaops.web.handler;

import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.typesafe.config.Config;
import ru.javaops.masterjava.config.Configs;
import ru.javaops.web.AuthUtil;

import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

public class SoapServerSecurityHandler extends SoapBaseHandler {
	private static final Config CONFIG = Configs.getConfig("hosts.conf", "hosts");
	private static final String AUTH_HEADER = AuthUtil.encodeBasicAuthHeader(CONFIG.getConfig("mail").getString("user"), CONFIG.getConfig("mail").getString("password"));

	@Override
	public boolean handleMessage(MessageHandlerContext context) {
		if (isInbound(context)) {
			Map<String, List<String>> headers = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);
			int code = AuthUtil.checkBasicAuth(headers, AUTH_HEADER);
			if (code != 0) {
				context.put(MessageContext.HTTP_RESPONSE_CODE, code);
				throw new SecurityException();
			}
		}
		return true;
	}

	@Override
	public boolean handleFault(MessageHandlerContext context) {
		return true;
	}
}
