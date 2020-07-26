package org.cattech.homeAutomation.WebInterfaceModule;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

public class WebServer extends HomeAutomationModule {

	private static final String CONFIG_WEBSERVER_PORT = "webServer.port";
	static final String CONFIG_WEBSERVER_WAR_FOLDER = "webServer.warFolder";

	static Logger log = Logger.getLogger(WebServer.class.getName());

	protected WebServer(ChannelController controller) {
		super(controller);

		Properties props = controller.getConfig().getProps();
		int port = Integer.valueOf(props.getProperty(CONFIG_WEBSERVER_PORT));

		Server server = new Server(port);

		ContextHandler aboutContext = new ContextHandler("/about/");
		aboutContext.setHandler(new AboutPage());

		ContextHandlerCollection contexts = new ContextHandlerCollection(aboutContext);

		File warFolder = new File(
				props.getProperty(CONFIG_WEBSERVER_WAR_FOLDER, System.getProperty("user.dir") + "/../"));

		File[] warFiles = warFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".war");
			}
		});

		if (warFiles == null) {
			try {
				log.error("Not starting webserver, did not find any warfiles in " + warFolder.getCanonicalPath());
			} catch (IOException e) {
				log.error("Could not determine war folder path.", e);
			}
		} else {

			for (int i = 0; i < warFiles.length; i++) {
				WebAppContext webapp = new WebAppContext();
				webapp.setWar(warFiles[i].getAbsolutePath());
				contexts.addHandler(webapp);
			}

			Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
			classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
					"org.eclipse.jetty.annotations.AnnotationConfiguration");

			server.setHandler(contexts);

			try {
				server.start();
				server.join();
			} catch (Exception e) {
				log.error("Webserver terminated.", e);
			} // Wait until the server stop serving, should be when the app closes.
		}
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getModuleChannelName() {
		return "WebServerModule";
	}

}
