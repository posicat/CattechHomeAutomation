package org.cattech.homeAutomation.deviceResolver;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.deviceHelpers.DeviceNameHelper;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacketHelper;
import org.json.JSONArray;
import org.json.JSONObject;

public class DeviceResolver extends HomeAutomationModule {
	static Logger log = LogManager.getLogger(DeviceResolver.class.getName());
	Hashtable<JSONObject, JSONArray> lookupTable = new Hashtable<JSONObject, JSONArray>();

	public DeviceResolver(ChannelController controller) {
		super(controller);
	}

	@Override
	public String getModuleChannelName() {
		return "DeviceResolver";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (incoming.hasData("resolution")) {
			String resolution = incoming.getDataString("resolution");
			if ("addLookup".equals(resolution)) {
				JSONObject nativeDevice = incoming.getDataJObj(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE);
				JSONArray commonDevice = incoming.getDataJArr(HomeAutomationPacket.FIELD_DATA_DEVICE);

				String lookup = DeviceNameHelper.addLookup(nativeDevice, commonDevice,lookupTable);
				HomeAutomationPacket reply = new HomeAutomationPacket();
				reply.putData("addLookupResult", lookup);
				outgoing.add(reply);
			}
			if ("toCommon".equals(resolution)) {
				lookupTable = new Hashtable<JSONObject, JSONArray>();
				DeviceNameHelper.loadDeviceMappings(db,lookupTable);

				log.debug("Resolving to common :"+incoming);
				Set<JSONArray> cDevs = new HashSet<JSONArray>();
				if (incoming.hasData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE)) {
					// Resolve native devices, then clear it out of the data
					cDevs = convertToCommonNames(incoming.getDataJObj(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE));
					incoming.removeFromData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE);

					for (JSONArray cDev : cDevs) {
						// Copy data from In to Out as we want to send back most of it.
						HomeAutomationPacket reply = HomeAutomationPacketHelper.generateReplyPacket(incoming,getModuleChannelName());

						// Remove output destinations, so we can set our own.
						reply.removeDestination();
						reply.addDestination(incoming.getDataString(HomeAutomationPacket.FIELD_DATA_POST_RESOLVE));
						reply.removeFromData(HomeAutomationPacket.FIELD_DATA_POST_RESOLVE);

						// Clear fields we don't need to send back from the DataOut
						reply.removeFromData(HomeAutomationPacket.FIELD_RESOLUTION);
						reply.removeFromData(HomeAutomationPacket.FIELD_DATA_DEVICE);
						
						reply.putData(HomeAutomationPacket.FIELD_DATA_DEVICE, cDev);
						outgoing.add(reply);
					}
				}
			}
			if ("toNative".equals(resolution)) {
				lookupTable = new Hashtable<JSONObject, JSONArray>();
				DeviceNameHelper.loadDeviceMappings(db,lookupTable);

				Set<JSONObject> nDevs = new HashSet<JSONObject>();
				if (incoming.hasData("device")) {

					String postResolve = null;
					if (incoming.hasData(HomeAutomationPacket.FIELD_DATA_POST_RESOLVE)) {
						postResolve = incoming.getDataString(HomeAutomationPacket.FIELD_DATA_POST_RESOLVE);
					}
					// Resolve native devices, then clear it out of the data
					nDevs = convertToNativeNames(incoming.getDataJArr("device"));

					log.debug("Found " + nDevs.size() + " native Devices.");
					for (JSONObject nDev : nDevs) {
						// Copy data from In to Out as we want to send back most of it.
						HomeAutomationPacket reply = HomeAutomationPacketHelper.generateReplyPacket(incoming,getModuleChannelName());

						// Clear fields we don't need to send back from the DataOut
						reply.removeFromData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE);
						reply.removeFromData("device");
						reply.removeFromData("resolution");
						
						
						reply.removeDestination();
						if (null == postResolve) {
							if (nDev.has("controlChannel")) {
								String controlChannel = nDev.getString("controlChannel");
								reply.addDestination(controlChannel);
							} else {
								log.error("Native device block doesn't have controChannel." + nDev);
							}
						} else {
							reply.addDestination(postResolve);
						}
						reply.removeFromData(HomeAutomationPacket.FIELD_DATA_POST_RESOLVE);
						reply.putData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE, nDev);
						outgoing.add(reply);
					}
				}
			}
		}
	}

	private Set<JSONObject> convertToNativeNames(JSONArray commonDevice) {
		Set<JSONObject> resultDevices = new HashSet<JSONObject>();

		for (JSONObject nDev : lookupTable.keySet()) {
			JSONArray cDev = lookupTable.get(nDev);

			if (DeviceNameHelper.commonDescriptorsMatch(cDev, commonDevice)) {
				resultDevices.add(nDev);
			}

		}

		return resultDevices;
	}

	private Set<JSONArray> convertToCommonNames(JSONObject nativeDevice) {
		Set<JSONArray> resultDevices = new HashSet<JSONArray>();

		for (JSONObject nDev : lookupTable.keySet()) {
			JSONArray cDev = lookupTable.get(nDev);
			if (DeviceNameHelper.nativeKeysMatch(nativeDevice, nDev)) {
				resultDevices.add(cDev);
			}
		}
		return resultDevices;
	}
}
