package ru.javaops.web.handler;


import com.typesafe.config.Config;
import org.slf4j.event.Level;
import ru.javaops.masterjava.config.Configs;

public class SoapServerLoggingHandler extends SoapLoggingHandler {
	private static final Config CONFIG = Configs.getConfig("hosts.conf", "hosts");

    public SoapServerLoggingHandler() {
        super(Level.valueOf(CONFIG.getConfig("mail").getString("debug.server")));
    }

    @Override
    protected boolean isRequest(boolean isOutbound) {
        return !isOutbound;
    }
}