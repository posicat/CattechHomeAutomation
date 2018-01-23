package org.cattech.homeAutomation.moduleBase;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeAutomationPacket {
	// Data packet field names - lowercase first letter by convention
	public static final String FIELD_REGISTER = "register";
	public static final String FIELD_NODE_NAME = "nodeName";
	public static final String FIELD_STATUS = "status";
	public static final String FIELD_SOURCE = "source";
	public static final String FIELD_DESTINATINO = "destination";
	public static final String FIELD_CHANNEL = "channel";
	public static final String FIELD_ALL_CHANNELS = "all_channels";
	public static final String FIELD_ERROR = "error";
	public static final String FIELD_RESOLUTION = "resolution";
	public static final String FIELD_DATA = "data";
	public static final String FIELD_DATA_NATIVE_DEVICE = "nativeDevice";
	public static final String FIELD_DATA_DEVICE = "device";
	public static final String FIELD_POST_RESOLVE = "postResolve";

	public static final String RESOLUTION_TO_COMMON = "toCommon";
	public static final String RESOLUTION_TO_NNATIVE = "toNative";

	// Channel constants - Capital first letter by convention
	public static final String CHANNEL_ALL = "All";
	public static final String CHANNEL_CONTROLLER = "ChannelController";
	public static final String CHANNEL_EVENT_HANDLER = "EventHandler";
	public static final String CHANNEL_DEVICE_RESOLVER = "DeviceResolver";

	private JSONObject wrapper = null;
	private JSONObject data = null;

	public HomeAutomationPacket() {
		this.wrapper = new JSONObject();
		this.data = new JSONObject();
	}

	public HomeAutomationPacket(String packet) {
		wrapper = new JSONObject(packet);
		if (wrapper.has(FIELD_DATA)) {
			data = wrapper.getJSONObject(FIELD_DATA);
		} else {
			data = new JSONObject();
		}
	}

	public String toString() {
		JSONObject toStr = HomeAutomationPacketHelper.copyJSONObject(wrapper);
		if (data != null && data.length() > 0) {
			JSONObject dataCopy = HomeAutomationPacketHelper.copyJSONObject(data);
			toStr.put(FIELD_DATA, dataCopy);
		}
		return toStr.toString();
	}

	// ====================================================================================================//
	// Simple access helper methods
	// ====================================================================================================//

	@Deprecated
	public JSONObject getData() {
		return data;
	}

	@Deprecated
	public JSONObject getWrapper() {
		return wrapper;
	}

	public void setDestination(String[] strings) {
		removeDestination();
		wrapper.put(FIELD_DESTINATINO, new JSONArray(strings));
	}

	public void setDestination(String string) {
		removeDestination();
		addDestination(string);
	}

	public void setSource(String value) {
		wrapper.put(FIELD_SOURCE, value);
	}

	public void addDestination(String channel) {
		if (!wrapper.has(FIELD_DESTINATINO)) {
			wrapper.put(FIELD_DESTINATINO, new JSONArray());
		}
		wrapper.getJSONArray(FIELD_DESTINATINO).put(channel);
	}

	public void removeDestination() {
		wrapper.remove(FIELD_DESTINATINO);
	}

	public void setData(JSONObject data) {
		this.data = data;
	}

	public void putWrapper(String key, Object value) {
		wrapper.put(key, value);
	}

	public void putData(String key, Object value) {
		data.put(key, value);
	}

	public void removeFromWrapper(String key) {
		wrapper.remove(key);
	}

	public void removeFromData(String key) {
		data.remove(key);
	}

	public boolean hasWrapper() {
		return (null != wrapper && wrapper.length() > 0);
	}

	public boolean hasData() {
		return (null != data && data.length() > 0);
	}

	public boolean hasWrapper(String key) {
		return wrapper.has(key);
	}

	public boolean hasData(String key) {
		return data.has(key);
	}

	public String getWrapperString(String key) {
		return wrapper.getString(key);
	}

	public String getDataString(String key) {
		return data.getString(key);
	}

	public JSONObject getWrapperJObj(String key) {
		return wrapper.getJSONObject(key);
	}

	public JSONObject getDataJObj(String key) {
		return data.getJSONObject(key);
	}

	public JSONArray getWrapperJArr(String key) {
		return wrapper.getJSONArray(key);
	}

	public JSONArray getDataJArr(String key) {
		return data.getJSONArray(key);
	}
}
