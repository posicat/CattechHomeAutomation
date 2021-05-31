package org.cattech.HomeAutomation.RootInterface.socketServlets;

import javax.servlet.annotation.WebServlet;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.homeAutomationContext.HomeAutomationContextListener;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

@WebServlet(name = "debugMonitor", urlPatterns = { "/ws/debugMonitor" })
public class DebugMonitorServlet extends WebSocketServlet {
	private static final long serialVersionUID = 1L;
	private HomeAutomationContextListener homeAutoConfig;

	@Override
	public void configure(WebSocketServletFactory factory) {
		
		this.homeAutoConfig = (HomeAutomationContextListener) getServletContext().getAttribute(HomeAutomationContextListener.INTERFACE_CONTROLLER);

		ChannelController controller = homeAutoConfig.getChannelController();

		WebSocketCreator creator = new WebSocketCreator() {
			@Override
			public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
				DebugMonitor dbm = new DebugMonitor(controller);
				return dbm;
			}
		};

		factory.setCreator(creator);
		factory.register(DebugMonitor.class);
	}

}
