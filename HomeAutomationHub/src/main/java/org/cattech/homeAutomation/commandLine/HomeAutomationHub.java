package org.cattech.homeAutomation.commandLine;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.configuration.HomeAutomationConfigurationException;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.ModuleManager;
import org.cattech.homeAutomation.communicationHub.NodeSocketConnectionManager;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;

public class HomeAutomationHub {
	private static Logger log = Logger.getLogger(HomeAutomationHub.class);

	public static void main(String[] args) throws IOException, HomeAutomationConfigurationException {
		ChannelController controller =  new ChannelController(new HomeAutomationConfiguration());

		ModuleManager loader;
		try {
			loader = new ModuleManager(controller.getConfig());
			List<HomeAutomationModule> modules = loader.findLoadableModules(controller);

			for (HomeAutomationModule mod : modules) {
				String moduleChannelName = mod.getModuleChannelName();
				new Thread(mod, moduleChannelName).start();
			}
		} catch (Exception e) {
			log.error("Could not initialize module loader, skipping module load.", e);
		}

		NodeSocketConnectionManager server = new NodeSocketConnectionManager(controller);
		new Thread(server, "Socket Connection Manager").start();

		try {
			Thread.sleep(2000);

			log.info("Listening . . .\r\n\r\n");
			while (!server.isStopped()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			log.error("InterruptedException : ",e);
		}
		log.info("Stopping Server");
		server.stop();
	}

}
