package org.cattech.homeAutomation.communicationHub;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.configuration.HomeAutomationConfigurationException;
import org.cattech.homeAutomation.configuration.homeAutomationConfiguration;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;

public class Hub {
	private static Logger log = Logger.getLogger(Hub.class);

	private static ChannelController controller = null;

	public static void main(String[] args) throws IOException, HomeAutomationConfigurationException {
		controller = new ChannelController(new homeAutomationConfiguration());

		NodeSocketConnectionManager server = new NodeSocketConnectionManager(controller);
		new Thread(server, "Socket Connection Manager").start();

		ModuleManager loader;
		try {
			loader = new ModuleManager(controller.getConfig());

			List<HomeAutomationModule> modules = loader.findLoadableModules(controller);
			
			for (HomeAutomationModule mod : modules) {
				new Thread(mod, "Module : " + mod.getModuleChannelName()).start();
			}
		} catch (Exception e) {
			log.error("Could not initialize module loader, skipping module load.",e);
		}
				

		log.info("\r\n\r\nListening . . .");
		try {
			while (!server.isStopped()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("Stopping Server");
		server.stop();
	}

	

}
