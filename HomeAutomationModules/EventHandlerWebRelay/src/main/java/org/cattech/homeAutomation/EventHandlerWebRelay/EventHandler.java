package org.cattech.homeAutomation.EventHandlerWebRelay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
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
	protected void processMessage(HomeAutomationPacket hap) {
		InputStream is = null;

		List<JSONObject> actions = getActionsForEvent(hap, true);

		log.debug("Result of reactions : " + actions.toString());
		
		boolean handledInternally = true;
		for (JSONObject action : actions) {
			if (action.has("destination")) {
				action.put("source", "EventHandler");
				hubInterface.sendDataToController(action.toString());
			}else{
				handledInternally = false;
			}
		}
		
		if (!handledInternally) {
			
			hap.setDataOut(hap.getDataIn());
			hap.getDataOut().put("viaWeb", "true");
			hap.removeDestination();
			hap.addDestination("WebEventHandler");
			String event = hap.getReturnPacket();

			log.info("Forwarded to web eventHandler, something wasn't handled: " + event);
			URL url;
			try {
				url = new URL(urlPrefix + "?event=" + URLEncoder.encode(event, "UTF-8"));
				is = url.openStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				String line;
				while ((line = reader.readLine()) != null) {
					log.info("Response:" + line);
				}
			} catch (IOException e) {
				log.error("Error forwarding message to web event handler (why are we doing this?)", e);
			}
		}
	}

	private List<JSONObject> getActionsForEvent(HomeAutomationPacket hap, boolean limitToEarliestNext) {
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
						hap.getDataIn().getJSONArray("device"))
						&& triggerEvent.getString("action").equals(hap.getDataIn().getString("action"));
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
				log.debug("Processing : "+trigger.toString());
				JSONArray actions = trigger.getJSONArray("reactions");
				
				stmt = conn.createStatement();
				String query = "SELECT action FROM reactions WHERE reactions_id in (" + actions.join(",") + ")";

				log.debug("SQL : " + query);

				rs = stmt.executeQuery(query);
				while (rs.next()) {
					log.debug("Adding action : " + rs.getString("action"));
					JSONObject action = new JSONObject(rs.getString("action"));
					result.add(action);
				}
			} catch (SQLException e) {
				log.error("Error while reading data from reactions table.", e);
			}
		}
		// ---------- Done ----------

		closeNoThrow(conn);
		return result;
	}

	@Override
	public String getModuleChannelName() {
		return "EventHandler";
	}

}
