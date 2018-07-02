package org.cattech.homeAutomation.moduleBase;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeAutomationPacketHelper {

	public static HomeAutomationPacket generateReplyPacket(HomeAutomationPacket packet, String sourceChannel) {
		HomeAutomationPacket reply = new HomeAutomationPacket();

		if (packet.hasWrapper(HomeAutomationPacket.FIELD_SOURCE)) {
			// Generate the default return header
			JSONArray destination = new JSONArray();
			destination.put(packet.getWrapperString(HomeAutomationPacket.FIELD_SOURCE));
			reply.putWrapper(HomeAutomationPacket.FIELD_DESTINATION, destination);
			reply.putWrapper(HomeAutomationPacket.FIELD_SOURCE, sourceChannel);

			reply.setData(copyJSONObject(packet.getData()));
		}

		return reply;
	}

	public static JSONObject copyJSONObject(JSONObject jso) {
		if (jso==null) {
			return new JSONObject();
		}
		return new JSONObject(jso.toString());
	}

}
