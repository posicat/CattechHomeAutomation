package org.cattech.homeAutomation.moduleBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.configuration.homeAutomationConfiguration;

public abstract class HomeAutomationModule implements Runnable {
	protected Logger log = Logger.getLogger(this.getClass());
	protected NodeInterfaceString hubInterface;
	protected boolean running = false;
	protected homeAutomationConfiguration configuration;

	protected HomeAutomationModule(ChannelController controller) {
		log.info("--- Initializing module, channels : " + getModuleChannelName() + " ---");
		this.hubInterface = new NodeInterfaceString(controller);
		// log.info(getModuleChannelName());
		hubInterface.sendDataToController(
				"{\"register\":[\"" + getModuleChannelName() + "\"],\"nodeName\":\"" + getModuleChannelName() + "\"}");

		String response = null;
		while (null == response) {
			response = hubInterface.getDataFromController();
			sleepNoThrow(100);
		}
		log.info(response);
		// log.info(response);
		configuration = controller.getConfig();
	}

	protected abstract void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing);
	public abstract String getModuleChannelName();
	
	public void run() {
		running = true;
		while (running) {
			String packet = hubInterface.getDataFromController();
			if (null != packet) {
				log.info("Message : " + packet);

				HomeAutomationPacket incoming = new HomeAutomationPacket(this.getModuleChannelName(), packet);
				List<HomeAutomationPacket> outgoing = new ArrayList<HomeAutomationPacket>();
				processPacketRequest(incoming, outgoing);

				for (HomeAutomationPacket reply : outgoing) {
					try {
						hubInterface.sendDataToController(reply.toString());
						log.debug("Return packet" + reply);
					} catch (Exception e) {
						log.error("Error sending message back to node", e);
					}
				}

			} else {
				sleepNoThrow(1000);
			}
		}

	}

	public void sleepNoThrow(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e1) {
			// We don't care, just go on.
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	protected Connection getHomeAutomationDBConnection() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(configuration.getDBURL());
		} catch (SQLException ex) {
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return conn;
	}

	protected void closeNoThrow(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {
			log.error("Error closeing DB connection", e);
		}
	}
}
