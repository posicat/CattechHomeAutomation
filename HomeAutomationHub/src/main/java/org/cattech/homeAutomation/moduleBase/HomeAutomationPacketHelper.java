package org.cattech.homeAutomation.moduleBase;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeAutomationPacketHelper {

	public static HomeAutomationPacket generateReplyPacket(HomeAutomationPacket packet, String sourceChannel) {
		HomeAutomationPacket reply = new HomeAutomationPacket();

		if (packet.getWrapper().has(HomeAutomationPacket.PACKET_SOURCE)) {
			// Generate the default return header
			JSONArray destination = new JSONArray();
			destination.put(packet.getWrapper().get(HomeAutomationPacket.PACKET_SOURCE));
			reply.getWrapper().put(HomeAutomationPacket.PACKET_DESTINATION, destination);
			reply.getWrapper().put(HomeAutomationPacket.PACKET_SOURCE, sourceChannel);

			reply.setData(copyJSONObject(packet.getData()));
		}

		return reply;
	}

	public static JSONObject copyJSONObject(JSONObject jso) {
		return  new JSONObject(jso.toString());
	}

}
