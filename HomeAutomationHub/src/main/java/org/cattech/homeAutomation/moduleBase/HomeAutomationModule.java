package org.cattech.homeAutomation.moduleBase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class HomeAutomationModule implements Runnable {
	private static final int WAIT_TIME_BETWEEN_PACKET_CHECKS = 100;
	private Logger log = Logger.getLogger("HomeAutomationModule");
	protected NodeInterfaceString hubInterface;
	protected boolean running = false;
	protected HomeAutomationConfiguration configuration;

	protected HomeAutomationModule(ChannelController controller) {
		configuration = controller.getConfig();

		log.info("--- Initializing module, channels : " + getModuleChannelName() + " ---");
		hubInterface = new NodeInterfaceString(controller);
		HomeAutomationPacket hap = new HomeAutomationPacket(
				"{\"register\":[\"" + getModuleChannelName() + "\"],\"nodeName\":\"" + getModuleChannelName() + "\"}");
		hubInterface.sendDataPacketToController(hap);

		HomeAutomationPacket response = null;
		while (null == response) {
			log.info(getModuleChannelName() + " waiting");
			sleepNoThrow(100);
			response = hubInterface.getDataPacketFromController();
		}
		log.info(getModuleChannelName() + " started");

	}

	protected abstract void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing);

	public abstract String getModuleChannelName();

	@Override
	public void run() {
		running = true;
		while (this.running) {
			checkForIncomingPacket();
		}

	}

	public boolean autoStartModule() {
		if (configuration.isSkipModule(this.getModuleChannelName())) {
			log.debug("Skipping " + this.getModuleChannelName() + " due to skip in configuration file");
			return false;
		}
		return true;
	}

	protected void sleepNoThrowWhileWatchingForIncomingPackets(int sleepThisLong) {
		// Sleep for a specific amount of time, but keep checking packets
		// Intermittently. Rounds to the nearest second effectively.
		long doneAt = System.currentTimeMillis() + sleepThisLong;
		while (doneAt > System.currentTimeMillis()) {
			checkForIncomingPacket();
		}
	}

	protected void checkForIncomingPacket() {
		HomeAutomationPacket incoming = hubInterface.getDataPacketFromController();

		if (null != incoming) {
			log.debug("Incoming packet:" + incoming.toString());
			List<HomeAutomationPacket> outgoing = new ArrayList<HomeAutomationPacket>();
			try {
				processPacketRequest(incoming, outgoing);
			} catch (Exception e) {
				log.error("Error occured while processing packet", e);
			} finally {
				processOutgoingPackets(outgoing);
			}
		} else {
			sleepNoThrow(WAIT_TIME_BETWEEN_PACKET_CHECKS);
		}
	}

	public void processOutgoingPackets(List<HomeAutomationPacket> outgoing) {
		for (HomeAutomationPacket reply : outgoing) {
			try {
				hubInterface.sendDataPacketToController(reply);
			} catch (Exception e) {
				log.error("Error sending message back to node", e);
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
			log.error("Error getting sql connection to :" + configuration.getDBURL(), ex);
			log.error("SQLState: " + ex.getSQLState());
			log.error("VendorError: " + ex.getErrorCode());
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

	protected Stack<JSONObject> getActionsForReactions(Connection conn, List<JSONObject> reactions) {
		Stack<JSONObject> result = new Stack<JSONObject>();
		for (JSONObject reaction : reactions) {
			result.addAll(getActionsForReaction(conn, reaction));
		}
		return result;
	}

	protected Stack<JSONObject> getActionsForReaction(Connection conn, JSONObject reaction) {
		Stack<JSONObject> result = new Stack<JSONObject>();
		Statement stmt;
		ResultSet rs;
		log.debug("triggerMatches : " + reaction);
		if (reaction != null && reaction.has("reactions")) {
			try {
				// log.debug("Processing : " + trigger.toString());
				JSONArray reactionIDs = reaction.getJSONArray("reactions");

				stmt = conn.createStatement();
				String query = "SELECT action FROM reactions WHERE reactions_id in (" + reactionIDs.join(",") + ")";

				log.debug("SQL : " + query);

				rs = stmt.executeQuery(query);
				while (rs.next()) {
					log.debug("Adding action : " + rs.getString("action"));
					JSONObject action = new JSONObject(rs.getString("action"));
					result.add(action);
					log.error("Action added." + action);
				}
			} catch (SQLException | JSONException e) {
				log.error("Error while reading data from reactions table.", e);
			}
		}
		return result;
	}

	protected void writeFile(File authCache, String data) throws IOException {
		FileUtils.writeStringToFile(authCache, data, "UTF-8");

	}

	protected String loadFile(File authCache) throws IOException {
		String data = FileUtils.readFileToString(authCache, "UTF-8");
		return data;
	}

}
