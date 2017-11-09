package org.cattech.homeAutomation.EventHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Stack;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.deviceHelpers.DeviceNameHelper;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EventHandler extends HomeAutomationModule {
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

		log.debug("EvemtHandler request : " + incoming);
		
		List<JSONObject> actions = getActionsForEvent(incoming, true);

		log.debug("Result of reactions : " + actions.toString());

		for (JSONObject action : actions) {
			if (action.has("destination")) {
				action.put("source", "EventHandler");
				log.error("Action away..."+action);
				hubInterface.sendDataToController(action.toString());
			} else {
				log.error("Action has no destination."+action);
			}
		}
	}

	private List<JSONObject> getActionsForEvent(HomeAutomationPacket incoming, boolean limitToEarliestNext) {
		Connection conn = getHomeAutomationDBConnection();
		Statement stmt;
		ResultSet rs;
		List<JSONObject> triggerMatches = new Stack<JSONObject>();
		List<JSONObject> result = new Stack<JSONObject>();

		try {
			stmt = conn.createStatement();
			String query = "SELECT event,action,triggers_id FROM triggers ";
			if (limitToEarliestNext) {
				query += " WHERE earliestNext <= NOW()";
			}
			log.debug("SQL : " + query);
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				JSONObject triggerEvent = new JSONObject(rs.getString("event"));
				boolean match = DeviceNameHelper.commonDescriptorsMatch(triggerEvent.getJSONArray("device"),
						incoming.getData().getJSONArray("device"))
						&& triggerEvent.getString("action").equals(incoming.getData().getString("action"));
				if (match) {
					log.debug("Matched : " + rs.getString("event"));
					triggerMatches.add(new JSONObject(rs.getString("action")));
				} else {
					log.debug("No Match : " + rs.getString("event"));
				}
			}
		} catch (SQLException e) {
			log.error("Error while reading data from triggers table.", e);
		}

		// ---------- Have triggers, now find matching actions ----------

		for (JSONObject trigger : triggerMatches) {
			try {
//				log.debug("Processing : " + trigger.toString());
				JSONArray actions = trigger.getJSONArray("reactions");

				stmt = conn.createStatement();
				String query = "SELECT action FROM reactions WHERE reactions_id in (" + actions.join(",") + ")";

//				log.debug("SQL : " + query);

				rs = stmt.executeQuery(query);
				while (rs.next()) {
					log.debug("Adding action : " + rs.getString("action"));
					JSONObject action = new JSONObject(rs.getString("action"));
					result.add(action);
					log.error("Action added."+action);
				}
			} catch (SQLException|JSONException e) {
				log.error("Error while reading data from reactions table.", e);
			}
		}
		// ---------- Done ----------
		log.error("Action done");

		closeNoThrow(conn);
		return result;
	}
}
