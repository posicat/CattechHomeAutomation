package org.cattech.HomeAutomation.TradfriInterfaceHandler;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONObject;

import nl.stijngroenen.tradfri.device.Device;
import nl.stijngroenen.tradfri.device.Gateway;
import nl.stijngroenen.tradfri.device.event.DeviceEvent;
import nl.stijngroenen.tradfri.device.event.EventHandler;
import nl.stijngroenen.tradfri.device.event.LightChangeEvent;
import nl.stijngroenen.tradfri.device.event.MotionSensorEvent;
import nl.stijngroenen.tradfri.device.event.PlugChangeEvent;
import nl.stijngroenen.tradfri.device.event.RemoteEvent;
import nl.stijngroenen.tradfri.util.Credentials;

public class TradfriInterfaceHandler extends HomeAutomationModule {
	static Logger log = Logger.getLogger(TradfriInterfaceHandler.class.getName());

	private static final String TRADFRI_HANDLER = "TradfriInterfaceHandler";

	public TradfriInterfaceHandler(ChannelController controller) {
		super(controller);
		log.info("Beginning connection to Tradfri hub");

		Gateway gateway = new Gateway("10.0.0.104");
		Credentials credentials = gateway.connect("x");
		String identity = credentials.getIdentity();
		String key = credentials.getKey();

		Device[] devices = gateway.getDevices();
		log.info("Connected, there are " + devices.length + " devices registered with this hub");

		for (Device device : devices) {

			EventHandler<LightChangeEvent> eventHandler = new EventHandler<LightChangeEvent>() {
				@Override
				public void handle(LightChangeEvent event) {
					handleDeviceEvent(event);
				}
			};
			device.enableObserve(); // This is necessary for the event handler to work.
			device.addEventHandler(eventHandler);

		}

	}

	private void handleDeviceEvent(DeviceEvent event) {
		;

		Device device = event.getDevice();

		HomeAutomationPacket readPacket = new HomeAutomationPacket();
		readPacket.setSource(TRADFRI_HANDLER);
		readPacket.setDestination(HomeAutomationPacket.CHANNEL_EVENT_HANDLER);
		readPacket.setData(new JSONObject("{}"));
		JSONObject nativeDevice = new JSONObject();
		readPacket.putData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE, nativeDevice);

		System.out.println(device.getName() + " produced an event");
		if (event.getClass() == LightChangeEvent.class) {
			nativeDevice.put("type", "Light");
			nativeDevice.put("name", device.getName());
			LightChangeEvent lce = (LightChangeEvent) event;
			pushFieldsToData(lce.getNewProperties(), lce.getOldProperties(), readPacket.getData());
		}
		if (event.getClass() == PlugChangeEvent.class) {
			nativeDevice.put("type", "Plug");
		}
		if (event.getClass() == RemoteEvent.class) {
			nativeDevice.put("type", "Remote");
		}
		if (event.getClass() == MotionSensorEvent.class) {
			nativeDevice.put("type", "MotionSensor");
		}

		log.debug(hubInterface);
		hubInterface.sendDataPacketToController(readPacket);

	}

	private void pushFieldsToData(Object newProperties, Object oldProperties, JSONObject data) {
		try {
			Field[] propFields = newProperties.getClass().getDeclaredFields();
			for (int i = 0; i < propFields.length; i++) {
				propFields[i].setAccessible(true);
				String newV = String.valueOf(propFields[i].get(newProperties));
				String oldV = String.valueOf(propFields[i].get(oldProperties));
				if (! newV.equals(oldV) ) {
					data.put(propFields[i].getName(), newV);
				}
			}
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			log.error("Trouble reading newProperties off of DeviceEvent", e);
		}
	}

	@Override
	public String getModuleChannelName() {
		return "IkeaTradfriController";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (!incoming.hasWrapper("data")) {
			log.error("Packet has no data element. " + incoming);
		} else {
			JSONObject device = incoming.getDataJObj(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE);

		}
	}

}
