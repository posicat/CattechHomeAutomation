package org.cattech.HomeAutomation.homeAutomationContext;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;

public class HomeAutomationControllerRunnable implements Runnable {

	ChannelController controller = null;
	static Logger log = Logger.getLogger(HomeAutomationContextListener.class.getName());

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
