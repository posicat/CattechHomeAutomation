package org.cattech.HomeAutomation.TradfriInterfaceHandler;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import nl.stijngroenen.tradfri.device.Device;
import nl.stijngroenen.tradfri.device.Gateway;
import nl.stijngroenen.tradfri.device.Light;
import nl.stijngroenen.tradfri.device.LightProperties;
import nl.stijngroenen.tradfri.device.event.DeviceEvent;
import nl.stijngroenen.tradfri.device.event.EventHandler;
import nl.stijngroenen.tradfri.device.event.LightChangeEvent;
import nl.stijngroenen.tradfri.device.event.MotionSensorEvent;
import nl.stijngroenen.tradfri.device.event.PlugChangeEvent;
import nl.stijngroenen.tradfri.device.event.RemoteEvent;
import nl.stijngroenen.tradfri.util.Credentials;

public class TradfriInterfaceHandler extends HomeAutomationModule {
	private static final String TRADFRI_HANDLER = "TradfriInterfaceHandler";
	private static final String DATA_FIELD_NATIVE_NAME = "name";
	private static final String DATA_FIELD_NATIVE_ID = "ID";
	private static final String DATA_FIELD_NATIVE_TYPE = "type";
	private static final String TYPE_MOTION_SENSOR = "MotionSensor";
	private static final String TYPE_REMOTE = "Remote";
	private static final String TYPE_PLUG = "Plug";
	private static final String TYPE_LIGHT = "Light";
	private static final String CONFIG_TRADFRI_ADDRESS = "tradfri.address";
	private static final String CONFIG_TRADFRI_SECURITY = "tradfri.security";

	static Logger log = Logger.getLogger(TradfriInterfaceHandler.class.getName());
	private Gateway gateway;

	public TradfriInterfaceHandler(ChannelController controller) {
		super(controller);

		Properties props = controller.getConfig().getProps();
		String addr = props.getProperty(CONFIG_TRADFRI_ADDRESS);
		String security = props.getProperty(CONFIG_TRADFRI_SECURITY);

		gateway = new Gateway(addr);
		Credentials credentials = gateway.connect(security);
		String identity = credentials.getIdentity();
		String key = credentials.getKey();

		Device[] devices = gateway.getDevices();
		log.info("Tradfri Connected.  Found " + devices.length + " devices registered with the hub at " + addr);

		for (Device device : devices) {

			EventHandler<LightChangeEvent> lightEventHandler = new EventHandler<LightChangeEvent>() {
				@Override public void handle(LightChangeEvent event) {handleDeviceEvent(event);}
			};
			EventHandler<PlugChangeEvent> plugEventHandler = new EventHandler<PlugChangeEvent>() {
				@Override public void handle(PlugChangeEvent event) {handleDeviceEvent(event);}
			};
			EventHandler<RemoteEvent> remoteEventHandler = new EventHandler<RemoteEvent>() {
				@Override public void handle(RemoteEvent event) {handleDeviceEvent(event);}
			};
			EventHandler<MotionSensorEvent> motionEventHandler = new EventHandler<MotionSensorEvent>() {
				@Override public void handle(MotionSensorEvent event) {handleDeviceEvent(event);}
			};
			device.enableObserve(); // This is necessary for the event handler to work.
			device.addEventHandler(lightEventHandler);
			device.addEventHandler(plugEventHandler);
			device.addEventHandler(remoteEventHandler);
			device.addEventHandler(motionEventHandler);

		}

	}

	private void handleDeviceEvent(DeviceEvent event) {
		Device device = event.getDevice();

		HomeAutomationPacket readPacket = new HomeAutomationPacket();
		readPacket.setSource(TRADFRI_HANDLER);
		readPacket.setDestination(HomeAutomationPacket.CHANNEL_EVENT_HANDLER);
		readPacket.setData(new JSONObject("{}"));
		JSONObject nativeDevice = new JSONObject();
		JSONObject action = new JSONObject();


		System.out.println(device.getName() + " produced an event");
		if (event.getClass() == LightChangeEvent.class) {
			nativeDevice.put(DATA_FIELD_NATIVE_TYPE, TYPE_LIGHT);
			nativeDevice.put(DATA_FIELD_NATIVE_NAME, device.getName());
			nativeDevice.put(DATA_FIELD_NATIVE_ID, device.getInstanceId());
			LightChangeEvent lce = (LightChangeEvent) event;
			pushFieldsToDataAndAction(lce.getNewProperties(), lce.getOldProperties(), readPacket.getData());
		}
		if (event.getClass() == PlugChangeEvent.class) {
			nativeDevice.put(DATA_FIELD_NATIVE_TYPE, TYPE_PLUG);
			nativeDevice.put(DATA_FIELD_NATIVE_NAME, device.getName());
			nativeDevice.put(DATA_FIELD_NATIVE_ID, device.getInstanceId());
			PlugChangeEvent pce = (PlugChangeEvent) event;
			pushFieldsToDataAndAction(pce.getNewProperties(), pce.getOldProperties(), readPacket.getData());

		}
		if (event.getClass() == RemoteEvent.class) {
			nativeDevice.put(DATA_FIELD_NATIVE_TYPE, TYPE_REMOTE);
			nativeDevice.put(DATA_FIELD_NATIVE_NAME, device.getName());
			nativeDevice.put(DATA_FIELD_NATIVE_ID, device.getInstanceId());
			RemoteEvent rev = (RemoteEvent) event;
//			pushFieldsToData(rev.getNewProperties(), rev.getOldProperties(), readPacket.getData());
			System.out.println(rev);
		}
		if (event.getClass() == MotionSensorEvent.class) {
			nativeDevice.put(DATA_FIELD_NATIVE_TYPE, TYPE_MOTION_SENSOR);
			nativeDevice.put(DATA_FIELD_NATIVE_NAME, device.getName());
			nativeDevice.put(DATA_FIELD_NATIVE_ID, device.getInstanceId());
			MotionSensorEvent mse = (MotionSensorEvent) event;
//			pushFieldsToData(mse.getNewProperties(), mse.getOldProperties(), readPacket.getData());
			System.out.println(mse);
		}
		log.debug(hubInterface);
		hubInterface.sendDataPacketToController(readPacket);
	}

	private void pushFieldsToDataAndAction(Object newProperties, Object oldProperties, JSONObject data) {
		try {

			// Careful with this code com.google.gson is used to serialize the properties, and org.json is used by the home automation code
			Gson gson = new Gson();
			JsonObject jsonNew = gson.toJsonTree(newProperties).getAsJsonObject();
			JsonObject jsonOld = gson.toJsonTree(oldProperties).getAsJsonObject();
			
			Set<Entry<String, JsonElement>> newES = jsonNew.entrySet();
			
			JSONObject state= new JSONObject();
			JSONObject action= new JSONObject();
			for (Entry<String, JsonElement> entry : newES) {
				String newV = entry.getValue().toString();
				String oldV = jsonOld.getAsJsonPrimitive(entry.getKey()).toString();
				
				state.put(entry.getKey(), newV);
				if (!newV.equals(oldV)) {
					action.put(entry.getKey(), newV);
				}
			}
			data.put(HomeAutomationPacket.FIELD_DATA_STATE, state);
			data.put(HomeAutomationPacket.FIELD_DATA_ACTION, action);
		} catch (SecurityException | IllegalArgumentException e) {
			log.error("Trouble reading newProperties off of DeviceEvent", e);
		}
	}

	@Override
	public String getModuleChannelName() {
		return TRADFRI_HANDLER;
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (!incoming.hasWrapper("data")) {
			log.error("Packet has no data element. " + incoming);
		} else {
			JSONObject natDevice = incoming.getDataJObj(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE);

			String name = natDevice.getString(DATA_FIELD_NATIVE_NAME);
			String type = natDevice.getString(DATA_FIELD_NATIVE_TYPE);
//			String id = natDevice.getString(DATA_FIELD_NATIVE_ID);

			Device[] devices = gateway.getDevices();

			for (Device device : devices) {
				if (name.equals(device.getName())) {

					if (type.equals(TYPE_LIGHT)) {
						Light light = device.toLight();
						JSONObject action = incoming.getData().getJSONObject(HomeAutomationPacket.FIELD_DATA_ACTION);
						Gson gson = new Gson();
						LightProperties props = gson.fromJson(action.toString(),LightProperties.class);
						light.setProperties(props);
						try {
							Field newProp = light.getClass().getDeclaredField("newProperties");
							newProp.setAccessible(true);
							newProp.set(light, props);
						} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
							log.error("Error setting newProperties by reflection.",e);
						}
						light.applyUpdates();
					}

				}

			}

		}
	}

}
