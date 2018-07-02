package org.cattech.homeAutomation.EventHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.deviceHelpers.DeviceNameHelper;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacketHelper;
import org.cattech.homeAutomation.watchCat.WatchCatDatabaseHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class EventHandler extends HomeAutomationModule {
	static Logger log = Logger.getLogger(EventHandler.class.getName());
	private String urlPrefix;

	public EventHandler(ChannelController controller) {
		super(controller);

		final String baseUrl = controller.getConfig().getBaseURL();
		this.urlPrefix = baseUrl + "eventHandler.cgi";
		log.info("Enabling webrelay to " + this.urlPrefix);
	}

	@Override
	public String getModuleChannelName() {
		return "EventHandler";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (null != incoming.getWrapper()) {
			if (!incoming.hasWrapper("data")) {
				log.error("Packet has no data element. " + incoming);
			} else {
				HomeAutomationPacket reply = HomeAutomationPacketHelper.generateReplyPacket(incoming, getModuleChannelName());

				reply.setData(incoming.getData());
				if (incoming.hasData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE)) {
					reply.putData(HomeAutomationPacket.FIELD_RESOLUTION, HomeAutomationPacket.RESOLUTION_TO_COMMON);
					reply.putData(HomeAutomationPacket.FIELD_DATA_POST_RESOLVE, HomeAutomationPacket.CHANNEL_EVENT_HANDLER);
					reply.removeDestination();
					reply.setDestination(new String[] { HomeAutomationPacket.CHANNEL_DEVICE_RESOLVER });
				} else {
					List<JSONObject> actions = getActionsForEvent(incoming);

					log.debug("Result of reactions : " + actions.toString());

					for (JSONObject action : actions) {
						if (action.has("destination")) {
							action.put("source", "EventHandler");
							log.info("Action away..." + action);
							HomeAutomationPacket hap = new HomeAutomationPacket(action.toString());
							hubInterface.sendDataPacketToController(hap);
						} else {
							log.error("Action has no destination." + action);
						}
					}
				}
				outgoing.add(reply);
			}
		}
	}

	public List<JSONObject> getActionsForEvent(HomeAutomationPacket incoming) {
		Connection conn = getHomeAutomationDBConnection();
		Statement stmt;
		ResultSet rs;
		List<JSONObject> triggerReactions = new Stack<JSONObject>();
		
		String eventSignature = WatchCatDatabaseHelper.generateEventSignature(configuration.getHost(), incoming.getData());
		Boolean afterMin = WatchCatDatabaseHelper.afterMinDelay(conn, eventSignature);

		log.info("Event signature:" + eventSignature);
		log.debug("afterMin =  " + afterMin);

		if (null == afterMin || afterMin.booleanValue()) {
			boolean foundMatch = false;
			try {
				stmt = conn.createStatement();
				String query = "SELECT event,reaction,triggers_id FROM triggers ";
				log.debug("SQL : " + query);
				rs = stmt.executeQuery(query);

				while (rs.next()) {
					JSONObject triggerEvent = new JSONObject(rs.getString("event"));
					boolean match = DeviceNameHelper.commonDescriptorsMatch(triggerEvent.getJSONArray(HomeAutomationPacket.FIELD_DATA_DEVICE), incoming.getDataJArr(HomeAutomationPacket.FIELD_DATA_DEVICE))
							&& triggerEvent.getString(HomeAutomationPacket.FIELD_DATA_ACTION).equals(incoming.getDataString(HomeAutomationPacket.FIELD_DATA_ACTION));
					if (match) {
						String reaction = rs.getString("reaction");
						log.debug("Matched : " + rs.getString("event") + "->" + reaction);
						if (null != reaction) {
							triggerReactions.add(new JSONObject(reaction));
							foundMatch = true;
						}
					} else {
						log.debug("No Match : " + rs.getString("event"));
					}
				}
			} catch (SQLException | JSONException e) {
				log.error("Error while reading data from triggers table.", e);
			}

			if (foundMatch) {
				WatchCatDatabaseHelper.updateEventOccurance(conn, eventSignature);
			}
		}

		List<JSONObject> result = getActionsForReactions(conn, triggerReactions);

		log.info("Action done");

		closeNoThrow(conn);
		return result;
	}
}
