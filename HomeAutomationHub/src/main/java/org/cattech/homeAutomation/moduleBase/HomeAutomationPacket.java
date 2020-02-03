package org.cattech.homeAutomation.moduleBase;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeAutomationPacket {
	static Logger log = Logger.getLogger(HomeAutomationPacket.class.getName());

	// Data packet field names - lowercase first letter by convention
	public static final String FIELD_REGISTER = "register";
	public static final String FIELD_NODE_NAME = "nodeName";
	public static final String FIELD_STATUS = "status";
	public static final String FIELD_SOURCE = "source";
	public static final String FIELD_DESTINATION = "destination";
	public static final String FIELD_CHANNEL = "channel";
	public static final String FIELD_ALL_CHANNELS = "all_channels";
	public static final String FIELD_ERROR = "error";
	public static final String FIELD_RESOLUTION = "resolution";
	public static final String FIELD_DATA = "data";
	public static final String FIELD_DATA_NATIVE_DEVICE = "nativeDevice";
	public static final String FIELD_DATA_DEVICE = "device";
	public static final String FIELD_DATA_POST_RESOLVE = "postResolve";
	public static final String FIELD_DATA_ACTION = "action";

	public static final String RESOLUTION_TO_COMMON = "toCommon";
	public static final String RESOLUTION_TO_NATIVE = "toNative";

	// Channel constants - Capital first letter by convention
	public static final String CHANNEL_ALL = "All";
	public static final String CHANNEL_CONTROLLER = "ChannelController";
	public static final String CHANNEL_EVENT_HANDLER = "EventHandler";
	public static final String CHANNEL_DEVICE_RESOLVER = "DeviceResolver";

	private JSONObject wrapper = null;
	private JSONObject data = null;

	public HomeAutomationPacket() {
		this.setWrapper(new JSONObject());
		this.setData(new JSONObject());
	}

	public HomeAutomationPacket(String packet) {
		try {
			this.setWrapper(new JSONObject(packet));
			this.setData(new JSONObject());
			if (getWrapper().has(FIELD_DATA)) {
				this.setData(getWrapper().getJSONObject(FIELD_DATA));
			}
		} catch (JSONException e) {
			log.error("Error in JSON format : " + packet, e);
		}
	}

	public String toString() {
		JSONObject toStr = HomeAutomationPacketHelper.copyJSONObject(getWrapper());
		JSONObject packetData = getData();
		if (packetData != null && packetData.length() > 0) {
			toStr.put(FIELD_DATA, HomeAutomationPacketHelper.copyJSONObject(packetData));
		}
	    return toStr.toString();
	}

	// ====================================================================================================//
	// Simple access helper methods
	// ====================================================================================================//

	public void setDestination(String[] strings) {
		removeDestination();
		getWrapper().put(FIELD_DESTINATION, new JSONArray(strings));
	}

	public void setDestination(String string) {
		removeDestination();
		addDestination(string);
	}

	public void setSource(String value) {
		getWrapper().put(FIELD_SOURCE, value);
	}

	public void addDestination(String channel) {
		if (!getWrapper().has(FIELD_DESTINATION)) {
			getWrapper().put(FIELD_DESTINATION, new JSONArray());
		}
		getWrapper().getJSONArray(FIELD_DESTINATION).put(channel);
	}

	public void removeDestination() {
		getWrapper().remove(FIELD_DESTINATION);
	}

	public void setData(JSONObject data) {
		this.data = data;
	}

	public void putWrapper(String key, Object value) {
		getWrapper().put(key, value);
	}

	public void putData(String key, Object value) {
		getData().put(key, value);
	}

	public void removeFromWrapper(String key) {
		getWrapper().remove(key);
	}

	public void removeFromData(String key) {
		getData().remove(key);
	}

	public boolean hasWrapper() {
		return (null != getWrapper() && getWrapper().length() > 0);
	}

	public boolean hasData() {
		return (null != getData() && getData().length() > 0);
	}

	public boolean hasWrapper(String key) {
		return getWrapper().has(key);
	}

	public boolean hasData(String key) {
		return getData().has(key);
	}

	public String getWrapperString(String key) {
		return getWrapper().getString(key);
	}

	public String getDataString(String key) {
		return getData().getString(key);
	}

	public JSONObject getWrapperJObj(String key) {
		return getWrapper().getJSONObject(key);
	}

	public JSONObject getDataJObj(String key) {
		return getData().getJSONObject(key);
	}

	public JSONArray getWrapperJArr(String key) {
		return getWrapper().getJSONArray(key);
	}

	public JSONArray getDataJArr(String key) {
		return getData().getJSONArray(key);
	}

	public JSONObject getData() {
		return data;
	}

	public JSONObject getWrapper() {
		return wrapper;
	}

	public void setWrapper(JSONObject wrapper) {
		this.wrapper = wrapper;
	}
}
