package org.cattech.homeAutomation.WebInterfaceModule;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.cattech.HomeAutomation.homeAutomationContext.HomeAutomationContextListener;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppLifeCycle;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.deploy.PropertiesConfigurationManager;
import org.eclipse.jetty.deploy.graph.Node;
import org.eclipse.jetty.deploy.providers.WebAppProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.Configuration;

public class WebServer extends HomeAutomationModule {

	private static final String WEB_SERVER_MODULE = "WebServerModule";
	private static final String CONFIG_WEBSERVER_PORT = "webServer.port";
	static final String CONFIG_WEBSERVER_WAR_FOLDER = "webServer.warFolder";

	static Logger log = Logger.getLogger(WebServer.class.getName());
	public static ClassLoader classLoader = HomeAutomationModule.class.getClassLoader();

	Server server;
	
	@Override
	public String getModuleChannelName() {
		return WEB_SERVER_MODULE;
	}
	
	public WebServer(ChannelController controller) {
		super(controller);

		Properties props = controller.getConfig().getProps();
		int port = Integer.valueOf(props.getProperty(CONFIG_WEBSERVER_PORT));

		server = new Server(port);

		// Add to allow JSPs to work
		Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
        classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration");

		ContextHandlerCollection contextCollection = new ContextHandlerCollection();

		// WAR loading mechanism
		WebAppProvider webAppProvider = new WebAppProvider();
		
		webAppProvider.setParentLoaderPriority(true);

		File warFolder = new File(props.getProperty(CONFIG_WEBSERVER_WAR_FOLDER, System.getProperty("user.dir") + "/../"));
        
		try {
			webAppProvider.setMonitoredDirName(warFolder.getCanonicalPath());
		} catch (IOException e1) {
			log.error("Error setting monitored folder : " + warFolder.toString(),e1);
		}
        webAppProvider.setScanInterval(1);
        webAppProvider.setConfigurationManager(new PropertiesConfigurationManager());
        webAppProvider.setExtractWars(true); 

        //Generic about page
        ContextHandler aboutContext = new ContextHandler("/about/");
		aboutContext.setHandler(new AboutPage());
		contextCollection.addHandler(aboutContext);
		
		// Setup the deployment manager to include our class for all deployments
        ContextAttributeCustomizer contextAttributeCustomizer = new ContextAttributeCustomizer();
        contextAttributeCustomizer.setAttribute(HomeAutomationContextListener.INTERFACE_CONTROLLER, new HomeAutomationContextListener(controller));

		DeploymentManager deployer = new DeploymentManager();
        deployer.addAppProvider(webAppProvider);
		deployer.addLifeCycleBinding(contextAttributeCustomizer);
        deployer.setContexts(contextCollection);
        deployer.setContextAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");
        		
        // Add WAR loader and context collection to the server, and then start it.
        server.addBean(deployer);

        server.setHandler(contextCollection);
        
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			log.error("Webserver terminated.", e);
		} // Wait until the server stop serving, should be when the app closes.
	}
	
	public static class ContextAttributeCustomizer implements AppLifeCycle.Binding
    {
        public final Map<String, Object> attributes = new HashMap<>();

        public void setAttribute(String name, Object value)
        {
            this.attributes.put(name, value);
        }

        @Override
        public String[] getBindingTargets()
        {
            return new String[]{ AppLifeCycle.DEPLOYING };
        }

        @Override
        public void processBinding(Node node, App app) throws Exception
        {
            ContextHandler handler = app.getContextHandler();
            if (handler == null)
            {
                throw new NullPointerException("No Handler created for App: " + app);
            }
            attributes.forEach((name, value) -> handler.setAttribute(name, value));
        }
    }

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		// TODO Auto-generated method stub

	}



}
