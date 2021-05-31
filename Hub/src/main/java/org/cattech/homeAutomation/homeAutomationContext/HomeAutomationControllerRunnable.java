package org.cattech.homeAutomation.homeAutomationContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;

public class HomeAutomationControllerRunnable implements Runnable {

	ChannelController controller = null;
	static Logger log = LogManager.getLogger(HomeAutomationContextListener.class.getName());

	public HomeAutomationControllerRunnable(ChannelController controller) {
		this.controller=controller;
	}

	@Override
	public void run() {
	}

	public void terminate() {	
	}

	public HomeAutomationConfiguration getConfiguration() {
		return controller.getConfig();
	}

}
