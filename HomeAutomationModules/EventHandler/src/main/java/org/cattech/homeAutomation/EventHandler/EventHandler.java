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

		log.debug("EventHandler request : " + incoming);

		if (null != incoming.getWrapper()) {
			if (!incoming.getWrapper().has("data")) {
				log.error("Packet has no data element. " + incoming);
			} else {
				HomeAutomationPacket reply = HomeAutomationPacketHelper.generateReplyPacket(incoming,
						getModuleChannelName());

				reply.setData(incoming.getData());
				if (incoming.getData().has("nativeDevice")) {
					reply.getData().put("resolution", "toCommon");
					reply.getData().put("postResolv", "EventHandler");
					reply.getWrapper().remove("destination");
					reply.getWrapper().put("destination", new String[] { "DeviceResolver" });
				} else {
					List<JSONObject> actions = getActionsForEvent(incoming);

					log.debug("Result of reactions : " + actions.toString());

					for (JSONObject action : actions) {
						if (action.has("destination")) {
							action.put("source", "EventHandler");
							log.error("Action away..." + action);
							hubInterface.sendDataToController(action.toString());
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

		String eventSignature = WatchCatDatabaseHelper.generateEventSignature(configuration.getHost(),incoming.getData());
		Boolean afterMin = WatchCatDatabaseHelper.afterMinDelay(conn, eventSignature);

		log.info("Event signature:" + eventSignature);
		log.debug("afterMin =  " + afterMin);

		if (null==afterMin || afterMin) {
			boolean foundMatch = false;
			try {
				stmt = conn.createStatement();
				String query = "SELECT event,reaction,triggers_id FROM triggers ";
				log.debug("SQL : " + query);
				rs = stmt.executeQuery(query);

				while (rs.next()) {
					JSONObject triggerEvent = new JSONObject(rs.getString("event"));
					boolean match = DeviceNameHelper.commonDescriptorsMatch(triggerEvent.getJSONArray("device"),
							incoming.getData().getJSONArray("device"))
							&& triggerEvent.getString("action").equals(incoming.getData().getString("action"));
					if (match) {
						log.debug("Matched : " + rs.getString("event"));
						triggerReactions.add(new JSONObject(rs.getString("reaction")));
						foundMatch = true;
					} else {
						log.debug("No Match : " + rs.getString("event"));
					}
				}
			} catch (SQLException e) {
				log.error("Error while reading data from triggers table.", e);
			}

			if (foundMatch) {
				WatchCatDatabaseHelper.updateEventOccurance(conn, eventSignature);
			}
		}

		List<JSONObject> result = getActionsForReactions(conn, triggerReactions);

		log.error("Action done");

		closeNoThrow(conn);
		return result;
	}

}
