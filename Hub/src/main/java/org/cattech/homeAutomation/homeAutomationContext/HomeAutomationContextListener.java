package org.cattech.homeAutomation.homeAutomationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;

public class HomeAutomationContextListener implements ServletContextListener {

	static Logger log = LogManager.getLogger(HomeAutomationContextListener.class.getName());

	public static final String INTERFACE_CONTROLLER = "homeAutomationController";
	public static final String INTERFACE_CONTROLLER_THREAD = "homeAutomationControllerThread";
	public static final String SERVER_MANIFEST = "webserverManifest";


	private Thread haControllerThread;
	private HomeAutomationControllerRunnable haControlerRunnable;

	private ChannelController controller;

	public HomeAutomationContextListener() {
	}

	public HomeAutomationContextListener(ChannelController controller) {
		this.controller = controller;
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		haControlerRunnable = new HomeAutomationControllerRunnable(controller);
		
		haControllerThread = new Thread(haControlerRunnable);
		haControllerThread.setDaemon(true);
		haControllerThread.start();
		
		ServletContext servCont = sce.getServletContext();
		servCont.setAttribute(INTERFACE_CONTROLLER_THREAD, haControllerThread);
		servCont.setAttribute(INTERFACE_CONTROLLER, haControlerRunnable);
		
		log.info("Web contexts initialized");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		haControlerRunnable.terminate();
		haControllerThread.interrupt();
	}
	
	public ChannelController getChannelController() {
		return controller;
	}

}
