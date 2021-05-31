package org.cattech.HomeAutomation.TradfriInterfaceHandler;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.deviceHelpers.DeviceNameHelper;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import nl.stijngroenen.tradfri.device.Device;
import nl.stijngroenen.tradfri.device.DeviceProperties;
import nl.stijngroenen.tradfri.device.DeviceType;
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
	private static final String COMMAND_ACTION = "action";
	private static final String TRADFRI_HANDLER = "TradfriInterfaceHandler";
	private static final String TRAFFRI_PROTOCOL = "tradfri";
	private static final String DATA_FIELD_NATIVE_NAME = "name";
	private static final String DATA_FIELD_NATIVE_ID = "ID";
	private static final String DATA_FIELD_NATIVE_TYPE = "type";
	private static final String DATA_FIELD_CONTROL_CHANNEL = "controlChannel";
	private static final String DATA_FIELD_PROTOCOL = "protocol";
	private static final String TYPE_MOTION_SENSOR = "MOTIONSENSOR";
	private static final String TYPE_REMOTE = "REMOTE";
	private static final String TYPE_PLUG = "PLUG";
	private static final String TYPE_LIGHT = "LIGHT";
	private static final String CONFIG_TRADFRI_ADDRESS = "tradfri.address";
	private static final String CONFIG_TRADFRI_SECURITY = "tradfri.security";
	private static final String DEVICE_AUTO_GENERATED = "!AutoGen";


	static Logger log = LogManager.getLogger(TradfriInterfaceHandler.class.getName());
	private Gateway gateway;

	public TradfriInterfaceHandler(ChannelController controller) {
		super(controller);
		NetworkConfig.createStandardWithoutFile(); // Keep a Californium.conf file from being generated, we don't need it.

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

		nativeDevice.put(DATA_FIELD_CONTROL_CHANNEL,this.getModuleChannelName());
		nativeDevice.put(DATA_FIELD_PROTOCOL,TRAFFRI_PROTOCOL);

		System.out.println(device.getName() + " produced an event");
		if (event.getClass() == LightChangeEvent.class) {
			nativeDevice.put(DATA_FIELD_NATIVE_TYPE, TYPE_LIGHT);
			nativeDevice.put(DATA_FIELD_NATIVE_NAME, device.getName());
			nativeDevice.put(DATA_FIELD_NATIVE_ID, device.getInstanceId().toString());
			LightChangeEvent lce = (LightChangeEvent) event;
			pushFieldsToDataAndAction(lce.getNewProperties(), lce.getOldProperties(), readPacket.getData());
		}
		if (event.getClass() == PlugChangeEvent.class) {
			nativeDevice.put(DATA_FIELD_NATIVE_TYPE, TYPE_PLUG);
			nativeDevice.put(DATA_FIELD_NATIVE_NAME, device.getName());
			nativeDevice.put(DATA_FIELD_NATIVE_ID, device.getInstanceId().toString());
			PlugChangeEvent pce = (PlugChangeEvent) event;
			pushFieldsToDataAndAction(pce.getNewProperties(), pce.getOldProperties(), readPacket.getData());

		}
		if (event.getClass() == RemoteEvent.class) {
			nativeDevice.put(DATA_FIELD_NATIVE_TYPE, TYPE_REMOTE);
			nativeDevice.put(DATA_FIELD_NATIVE_NAME, device.getName());
			nativeDevice.put(DATA_FIELD_NATIVE_ID, device.getInstanceId().toString());
			RemoteEvent rev = (RemoteEvent) event;
//			pushFieldsToData(rev.getNewProperties(), rev.getOldProperties(), readPacket.getData());
			System.out.println(rev);
		}
		if (event.getClass() == MotionSensorEvent.class) {
			nativeDevice.put(DATA_FIELD_NATIVE_TYPE, TYPE_MOTION_SENSOR);
			nativeDevice.put(DATA_FIELD_NATIVE_NAME, device.getName());
			nativeDevice.put(DATA_FIELD_NATIVE_ID, device.getInstanceId().toString());
			MotionSensorEvent mse = (MotionSensorEvent) event;
//			pushFieldsToData(mse.getNewProperties(), mse.getOldProperties(), readPacket.getData());
			System.out.println(mse);
		}
		readPacket.getData().put(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE,nativeDevice);
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
				String newVunQ = newV.replaceAll("(^\"|\"$)", "");
				if (!newV.equals(oldV)) {
					action.put(entry.getKey(), newVunQ);
				}
				state.put(entry.getKey(), newVunQ);
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
			Device[] devices = gateway.getDevices();

			try {
				JSONObject natDevice = incoming.getDataJObj(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE);
				String name = natDevice.getString(DATA_FIELD_NATIVE_NAME);
				String type = natDevice.getString(DATA_FIELD_NATIVE_TYPE);
	//			String id = natDevice.getString(DATA_FIELD_NATIVE_ID);
	
				for (Device device : devices) {
					if (name.equals(device.getName())) {
	
						if (type.equals(TYPE_LIGHT)) {
							Light light = device.toLight();
							JSONArray actions = incoming.getData().getJSONArray(HomeAutomationPacket.FIELD_DATA_ACTIONS);
							JSONObject tradfriActions = new JSONObject();

							// hopefully we'll get better control inside the Tradfri module and can eliminate this double JSON library.
							for(int i=0;i< actions.length();i++) {
								String[] action = actions.getString(i).split(":");
								if ("off".equals( action[0])) {tradfriActions.put("on","false");}
								if ("on".equals( action[0])) {tradfriActions.put("on","true");}
								if ("colourTemperature".equals( action[0])) {tradfriActions.put("colourTemperature",action[1]);}
								
								if ("dim".equals( action[0])) {AddCalculatedBrightness(device,tradfriActions,- Integer.valueOf(action[1]));}
								if ("bri".equals( action[0])) {AddCalculatedBrightness(device,tradfriActions,+ Integer.valueOf(action[1]));}
							} 

							Gson gson = new Gson();
							LightProperties props = gson.fromJson(tradfriActions.toString(),LightProperties.class);
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
			}catch(JSONException e) {
				// Ok if we don't find one
				e.printStackTrace();
			}
			try {
				JSONObject moduleCommand = incoming.getDataJObj(HomeAutomationPacket.FIELD_DATA_MODULE_COMMAND);
				if (null!=moduleCommand) {
					System.out.println("Command sent to module" + moduleCommand.toString());
					
					String action = moduleCommand.getString(COMMAND_ACTION);
					
					if ("syncDevices".equals(action)) {
						doSyncDevicesWithDatabase(devices);
					}
					
				}
			}catch(JSONException e) {
				// Ok if we don't find one
				e.printStackTrace();
			}
		}
	}

	private void AddCalculatedBrightness(Device device, JSONObject tradfriActions, int i) {
		// TODO Auto-generated method stub
		
	}

	private void doSyncDevicesWithDatabase(Device[] devices) {
		Hashtable<JSONObject, JSONArray> lookupTable = new Hashtable<JSONObject, JSONArray>();
		DeviceNameHelper.loadDeviceMappings(db,lookupTable);
		
		for (Device device : devices) {
			boolean found = false;
			for (JSONObject nDev : lookupTable.keySet()) {
				if (nDev.has(DATA_FIELD_PROTOCOL)) {
					if (nDev.getString(DATA_FIELD_PROTOCOL).equals(TRAFFRI_PROTOCOL)) {
						try {
							String name = nDev.getString(DATA_FIELD_NATIVE_NAME);
							if (name.equals(device.getName())) {
								found=true;
							}else {
								String id = nDev.getString(DATA_FIELD_NATIVE_ID);
								if(id.equals(String.valueOf(device.getInstanceId()))) {
									log.error("Entry "+nDev + " ID matches, but not name.  RENAMED");
								}
							}
						}catch (JSONException e) {
//							log.error("Trouble parsing JSON line :" + nDev,e);
						}
						
					}
				}
			}
			if (! found)  {
				System.out.println("Did not find entry for "+device.getName());
				JSONObject nativeDevice = new JSONObject();
				nativeDevice.put(DATA_FIELD_PROTOCOL, TRAFFRI_PROTOCOL);
				nativeDevice.put(DATA_FIELD_CONTROL_CHANNEL, TRADFRI_HANDLER);
				nativeDevice.put(DATA_FIELD_NATIVE_NAME, device.getName());
				nativeDevice.put(DATA_FIELD_NATIVE_TYPE, device.getType().toString());
				nativeDevice.put(DATA_FIELD_NATIVE_ID, String.valueOf(device.getInstanceId()));
				
				DeviceNameHelper.addDeviceMappingToDatbase(db,
						device.getName(),
						determineInterfaceType(device),
						"builtin/generic.png",
						"[\""+device.getName()+"\",\""+DEVICE_AUTO_GENERATED+"\"]",
						nativeDevice.toString()
						);
			}
		}
		
	}

	private String determineInterfaceType(Device device) {
		String type="unknown";
		if(device.getType().equals(DeviceType.LIGHT)) {
			type="lamp";
			DeviceProperties prop = device.getProperties();
			
			type+="_dimbri";
			type+="_cmww";
		}
		if(device.getType().equals(DeviceType.MOTION_SENSOR)) {
			type="motionSensor";
		}
		if(device.getType().equals(DeviceType.PLUG)) {
			type="outlet";
		}
		if(device.getType().equals(DeviceType.REMOTE)) {
			type="control";
		}
		return type;
	}



}
