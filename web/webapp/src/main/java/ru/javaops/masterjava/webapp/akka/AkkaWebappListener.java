package ru.javaops.masterjava.webapp.akka;

import lombok.extern.slf4j.Slf4j;
import ru.javaops.masterjava.akka.AkkaActivator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@WebListener
@Slf4j
public class AkkaWebappListener implements ServletContextListener {
	public static AkkaActivator akkaActivator;
	public static ExecutorService executorService;
	private static final int THREADS = 10;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        akkaActivator = AkkaActivator.start("WebApp", "webapp");
		executorService = Executors.newFixedThreadPool(THREADS);
	}

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        akkaActivator.shutdown();
		executorService.shutdown();
    }
}