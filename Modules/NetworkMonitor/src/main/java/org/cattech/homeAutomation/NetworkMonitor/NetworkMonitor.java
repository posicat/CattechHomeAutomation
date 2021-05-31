package org.cattech.homeAutomation.NetworkMonitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkMonitor extends HomeAutomationModule {
	static Logger log = LogManager.getLogger(NetworkMonitor.class.getName());
	private HashMap<String, NetworkDataVO> logLookup = new HashMap<String, NetworkDataVO>();

	public NetworkMonitor(ChannelController controller) {
		super(controller);

		loadKnownNetworkDevices();

		// We need to listen to all events to find out what to log, so listen to the
		// EventHandler
		HomeAutomationPacket hap = new HomeAutomationPacket("{\"register\":[\""
				+ HomeAutomationPacket.CHANNEL_EVENT_HANDLER + "\"],\"nodeName\":\"" + getModuleChannelName() + "\"}");
		hubInterface.sendDataPacketToController(hap);
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (incoming.hasWrapper()) {
			if (!incoming.hasData()) {
				log.error("Packet has no data element. " + incoming);
			} else {
				if (incoming.hasData(HomeAutomationPacket.FIELD_DATA_DEVICE)) {
					JSONArray deviceArray = incoming.getData().getJSONArray(HomeAutomationPacket.FIELD_DATA_DEVICE);

					log.debug("Checking packet for logging : " + incoming);
				}
			}
		}
	}

	private void parseARPTable() {

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("/proc/net/arp"));
			String line = reader.readLine();
			while (line != null) {
				NetworkDataVO nd = new NetworkDataVO(line);
				if (nd.mac != null) {
					if (!logLookup.containsKey(nd.mac)) {
						logLookup.put(nd.mac, nd);
					} else {
						logLookup.get(nd.mac).registerCurrentState(nd);
					}
				}
			}
			reader.close();
		} catch (Exception e) {
			log.error("Error reading ARP table", e);
		}
	}

	private String findStringInJsonByPath(JSONObject data, String path) {
		String found = null;
		if (null != path) {
			String[] pathArr = path.split("/", 2);

			try {

				if (pathArr.length > 1) {
					if (data.has(pathArr[0])) {
						found = findStringInJsonByPath(data.getJSONObject(pathArr[0]), pathArr[1]);
					}
				} else {
					if (data.has(path)) {
						found = data.get(path).toString();
					}
				}
			} catch (JSONException e) {
				// We won't match all JSON structures, if we don't, we simply return our default
				// value here.
			}
		}
		return found;
	}

	@Override
	public String getModuleChannelName() {
		return "DataLogger";
	}

	public void loadKnownNetworkDevices() {
		Connection conn = getHomeAutomationDBConnection();
		if (null == conn) {
			log.error("Could not obtain connection to HomeAutomation database");
			return;
		}

		this.logLookup = new HashMap<String, NetworkDataVO>();

		Statement stmt;
		ResultSet rs;
		try {
			stmt = conn.createStatement();
			String query = "SELECT networkNames_id, name, mac, lastSeen, monitorLevel FROM networkNames";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				if (null != rs.getString("networkNames_id")) {

					NetworkDataVO nd = new NetworkDataVO();
					nd.networkNames_id = rs.getInt("networkNames_id");
					nd.deviceName = rs.getString("name");
					nd.mac = rs.getString("mac");
					nd.lastSeen = rs.getString("lastSeen");
					nd.monitorLevel = rs.getString("monitorLevel");
					logLookup.put(nd.mac, nd);

				}
			}
		} catch (SQLException e) {
			log.error("Error occured while loading logging settings from datbase", e);
			e.printStackTrace();
		}

		closeNoThrow(conn);
	}

	void main( ) {
		parseARPTable();
		
		System.out.println("Done.");
	}

}
