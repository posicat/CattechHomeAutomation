package org.cattech.homeAutomation.commandLine;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.ModuleManager;
import org.cattech.homeAutomation.communicationHub.NodeSocketConnectionManager;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.configuration.HomeAutomationConfigurationException;

public class HomeAutomationHub {
	private static Logger log = LogManager.getLogger(HomeAutomationHub.class);

	public static void main(String[] args) throws IOException, HomeAutomationConfigurationException {
		ChannelController controller = new ChannelController(new HomeAutomationConfiguration());

		ModuleManager modManager = new ModuleManager(controller.getConfig());
		modManager.startLoadableModules(controller);
		
		NodeSocketConnectionManager server = new NodeSocketConnectionManager(controller);
		new Thread(server, "Socket Connection Manager").start();

		try {
			Thread.sleep(2000);

			log.info("Listening . . .\r\n\r\n");
			while (!server.isStopped()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			log.error("InterruptedException : ", e);
		}
		log.info("Stopping Server");
		server.stop();
	}

}
