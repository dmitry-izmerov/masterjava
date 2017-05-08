package ru.javaops.web.handler;

import com.sun.xml.ws.api.handler.MessageHandlerContext;
import ru.javaops.web.Statistics;

public class SoapStatisticsHandler extends SoapBaseHandler {

	private static final String START_TIME = "startTime";

	@Override
	public boolean handleMessage(MessageHandlerContext context) {
		if (isInbound(context)) {
			context.put(START_TIME, System.currentTimeMillis());
		} else {
			Statistics.count(context.getPort().getName().toString(), (Long) context.get(START_TIME), Statistics.RESULT.SUCCESS);
		}
		return true;
	}

	@Override
	public boolean handleFault(MessageHandlerContext context) {
		if (isOutbound(context)) {
			Statistics.count(context.getPort().getName().toString(), (Long) context.get(START_TIME), Statistics.RESULT.FAIL);
		}
		return true;
	}
}
