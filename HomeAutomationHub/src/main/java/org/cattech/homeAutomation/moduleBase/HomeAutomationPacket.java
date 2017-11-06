package org.cattech.homeAutomation.moduleBase;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeAutomationPacket {
	static final String PACKET_DATA = "data";
	static final String PACKET_SOURCE = "source";
	static final String PACKET_DESTINATION = "destination";

	private JSONObject wrapper = null;
	private JSONObject data = null;

	public HomeAutomationPacket(String returnPacketSource, String packet) {
		wrapper = new JSONObject(packet);
		if (wrapper.has(PACKET_DATA)) {
			data = wrapper.getJSONObject(PACKET_DATA);
		}
	}

	public HomeAutomationPacket() {
		this.wrapper = new JSONObject();
		this.data = new JSONObject();
	}

	public String toString() {
		JSONObject toStr = HomeAutomationPacketHelper.copyJSONObject(wrapper);
		JSONObject dataCopy = HomeAutomationPacketHelper.copyJSONObject(data);
		toStr.put(PACKET_DATA, dataCopy);
		return toStr.toString();
	}

	public JSONObject getData() {
		return data;
	}

	public JSONObject getWrapper() {
		return wrapper;
	}

	public boolean hasData() {
		return data.length() > 0;
	}

	public void addDestination(String channel) {
		if (!wrapper.has("destination")) {
			wrapper.put("destination", new JSONArray());
		}
		wrapper.getJSONArray("destination").put(channel);
	}

	public void removeDestination() {
		wrapper.remove("destination");
	}

	public void setData(JSONObject data) {
		this.data = data;
	}
}
