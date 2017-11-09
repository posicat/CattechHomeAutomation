package org.cattech.homeAutomation.deviceResolver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.deviceHelpers.DeviceNameHelper;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacketHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceResolver extends HomeAutomationModule {
	Hashtable<JSONObject, JSONArray> lookupTable = new Hashtable<JSONObject, JSONArray>();

	public DeviceResolver(ChannelController controller) {
		super(controller);
		loadDeviceMappings();
	}

	@Override
	public String getModuleChannelName() {
		return "DeviceResolver";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (incoming.getData().has("resolution")) {
			String resolution = incoming.getData().getString("resolution");
			if ("addLookup".equals(resolution)) {
				JSONObject nativeDevice = incoming.getData().getJSONObject("nativeDevice");
				JSONArray commonDevice = incoming.getData().getJSONArray("commonDevice");

				String lookup = addLookup(nativeDevice, commonDevice);
				HomeAutomationPacket reply = new HomeAutomationPacket();
				reply.getData().put("addLookupResult", lookup);
				outgoing.add(reply);
			}
			if ("toCommon".equals(resolution)) {
				Set<JSONArray> cDevs = new HashSet<JSONArray>();
				if (incoming.getData().has("nativeDevice")) {
					// Resolve native devices, then clear it out of the data
					cDevs = convertToCommonNames(incoming.getData().getJSONObject("nativeDevice"));
					incoming.getData().remove("nativeDevice");

					for (JSONArray cDev : cDevs) {
						// Copy data from In to Out as we want to send back most of it.
						HomeAutomationPacket reply = HomeAutomationPacketHelper.generateReplyPacket(incoming,getModuleChannelName());

						// Remove output destinations, so we can set our own.
						reply.removeDestination();
						reply.addDestination(incoming.getData().getString("postResolv"));

						// Clear fields we don't need to send back from the DataOut
						reply.getData().remove("postResolv");
						reply.getData().remove("resolution");
						reply.getData().remove("device");
						
						reply.getData().put("device", cDev);
						outgoing.add(reply);
					}
				}
			}
			if ("toNative".equals(resolution)) {
				Set<JSONObject> nDevs = new HashSet<JSONObject>();
				if (incoming.getData().has("device")) {

					String postResolv = null;
					if (incoming.getData().has("postResolv")) {
						postResolv = incoming.getData().getString("postResolv");
					}
					// Resolve native devices, then clear it out of the data
					nDevs = convertToNativeNames(incoming.getData().getJSONArray("device"));

					for (JSONObject nDev : nDevs) {
						// Copy data from In to Out as we want to send back most of it.
						HomeAutomationPacket reply = HomeAutomationPacketHelper.generateReplyPacket(incoming,getModuleChannelName());

						// Clear fields we don't need to send back from the DataOut
						reply.getData().remove("device");
						reply.getData().remove("postResolv");
						reply.getData().remove("resolution");
						
						
						reply.removeDestination();
						if (null == postResolv) {
							if (nDev.has("controlChannel")) {
								String controlChannel = nDev.getString("controlChannel");
								reply.addDestination(controlChannel);
							} else {
								log.error("Native device block doesn't have controChannel." + nDev);
							}
						} else {
							reply.addDestination(postResolv);
						}
						reply.getData().remove("nativeDevice");
						reply.getData().put("nativeDevice", nDev);
						outgoing.add(reply);
					}
				}
			}
		}
	}

	private Set<JSONObject> convertToNativeNames(JSONArray commonDevice) {
		Set<JSONObject> resultDevices = new HashSet<JSONObject>();

		for (JSONObject nDev : lookupTable.keySet()) {
			JSONArray cDev = lookupTable.get(nDev);

			if (DeviceNameHelper.commonDescriptorsMatch(cDev, commonDevice)) {
				resultDevices.add(nDev);
			}

		}

		return resultDevices;
	}

	private Set<JSONArray> convertToCommonNames(JSONObject nativeDevice) {
		Set<JSONArray> resultDevices = new HashSet<JSONArray>();

		for (JSONObject nDev : lookupTable.keySet()) {
			JSONArray cDev = lookupTable.get(nDev);
			if (DeviceNameHelper.nativeKeysMatch(nativeDevice, nDev)) {
				resultDevices.add(cDev);
			}
		}
		return resultDevices;
	}

	private String addLookup(JSONObject nativeDevice, JSONArray commonDevice) {
//		log.info("\tAdding lookup " + nativeDevice.toString() + " <--> " + commonDevice.toString());
		lookupTable.put(nativeDevice, commonDevice);
		return "successful";
	}

	public void loadDeviceMappings() {
		Connection conn = getHomeAutomationDBConnection();
		if (null == conn) {
			log.error("Could not obtain connection to HomeAutomation database");
			return;
		}

		Statement stmt;
		ResultSet rs;
		try {
			stmt = conn.createStatement();
			String query = "SELECT commonDevice,nativeDevice FROM deviceMap";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				try {
					JSONObject nativeDevice = new JSONObject(rs.getString("nativeDevice"));
					JSONArray commonDevice = new JSONArray(rs.getString("commonDevice"));
					addLookup(nativeDevice, commonDevice);
				} catch (JSONException je) {
					log.error("Error loading device mapping", je);
					log.error("nativeDevice : " + rs.getString("nativeDevice"));
					log.error("commonDevice : " + rs.getString("commonDevice"));
				}
			}
		} catch (SQLException e) {
			log.error("Error occured while loading device mappings from datbase", e);
			e.printStackTrace();
		}

	}

}
