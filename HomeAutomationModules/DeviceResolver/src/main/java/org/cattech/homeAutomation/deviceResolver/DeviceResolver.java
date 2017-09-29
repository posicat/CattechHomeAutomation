package org.cattech.homeAutomation.deviceResolver;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.json.JSONArray;
import org.json.JSONObject;

public class DeviceResolver extends HomeAutomationModule {
	Hashtable<JSONObject,JSONArray> lookupTable = new Hashtable<JSONObject,JSONArray>();

	public DeviceResolver(ChannelController controller) {
			super(controller);
		}

	@Override
	public void run() {
		running=true;
		while (running) {
			String data = hubInterface.getDataFromController();
			if (null!=data) {
				processMessage(data);
			}else{
				sleepNoThrow(1000);
			}
		}
	}

	private void processMessage(String packet) {
		log.fine("Message : "+packet);

		HomeAutomationPacket hap = new HomeAutomationPacket(this.getModuleChannelName(),packet);
		
		if (hap.getDataIn().has("resolution")) {
			String resolution = hap.getDataIn().getString("resolution");
			if ("addLookup".equals(resolution)) {
				JSONObject nativeDevice = hap.getDataIn().getJSONObject("nativeDevice");
				JSONArray commonDevice = hap.getDataIn().getJSONArray("commonDevice");
								
				String result = addLookup(nativeDevice,commonDevice);
				hap.getDataOut().put("addLookupResult", result);
			}
			if ("toCommon".equals(resolution)) {
				Set<JSONArray> cDevs = new HashSet<JSONArray>();
				if (hap.getDataIn().has("nativeDevice")) {

					// Copy data from In to Out as we want to send back most of it.
					hap.setDataOut(hap.getDataIn());
					// Remove output destinations, so we can set our own.
					hap.getOut().remove("destination");
					hap.addDestination(hap.getDataIn().getString("postResolv"));
					// Clear fields we don't need to send back from the DataOut
					hap.getDataOut().remove("postResolv");
					hap.getDataOut().remove("resolution");

					//Resolve native devices, then clear it out of the data
					cDevs = convertToCommonNames(hap.getDataIn().getJSONObject("nativeDevice"));
					hap.getDataOut().remove("nativeDevice");
					
					for (JSONArray cDev : cDevs) {
						hap.getDataOut().remove("device");
						hap.getDataOut().put("device",cDev);
						hubInterface.sendDataToController(hap.getReturnPacket());
					}
					hap.setDataOut(new JSONObject());
					
				}
			}
			if ("toNative".equals(resolution)) {
				Set<JSONObject> nDevs = new HashSet<JSONObject>();
				if (hap.getDataIn().has("device")) {

					// Copy data from In to Out as we want to send back most of it.
					hap.setDataOut(hap.getDataIn());
					// Remove output destinations, so we can set our own.
					hap.getOut().remove("destination");
					hap.addDestination(hap.getDataIn().getString("postResolv"));
					// Clear fields we don't need to send back from the DataOut
					hap.getDataOut().remove("postResolv");
					hap.getDataOut().remove("resolution");

					//Resolve native devices, then clear it out of the data
					nDevs = convertToNativeNames(hap.getDataIn().getJSONArray("device"));
					hap.getDataOut().remove("device");
					
					for (JSONObject nDev : nDevs) {
						hap.getDataOut().remove("nativeDevice");
						hap.getDataOut().put("nativeDevice",nDev);
						hubInterface.sendDataToController(hap.getReturnPacket());
					}
					hap.setDataOut(new JSONObject());
					
				}
			}
		}
	
		if (hap.hasReturnData()) {
			hubInterface.sendDataToController(hap.getReturnPacket());
		}
	}

	private Set<JSONObject> convertToNativeNames(JSONArray commonDevice) {
		Set<JSONObject> resultDevices= new HashSet<JSONObject>();
		
		for (JSONObject nDev : lookupTable.keySet()) {
			JSONArray cDev = lookupTable.get(nDev);

			if(commonDescriptorsMatch(cDev,commonDevice)) {
				resultDevices.add(nDev);
			}
			
		}
		
		return resultDevices;
	}

	private Set<JSONArray> convertToCommonNames(JSONObject nativeDevice) {
		Set<JSONArray> resultDevices= new HashSet<JSONArray>();
		
		for (JSONObject nDev : lookupTable.keySet()) {
			JSONArray cDev = lookupTable.get(nDev);
			if(nativeKeysMatch(nDev,nativeDevice)){
				resultDevices.add(cDev);
			}
		}
		return resultDevices;
	}

	private boolean nativeKeysMatch(JSONObject mightMatch, JSONObject toMatch) {
		int keysMatched = 0;
		for (String key : toMatch.keySet()) {
			if( mightMatch.has(key) && mightMatch.get(key).equals(toMatch.get(key)) ) {
				keysMatched++;
			}
		}
		return (keysMatched == toMatch.length());
	}

	private boolean commonDescriptorsMatch(JSONArray mightMatch, JSONArray toMatch) {
		int keysMatched = 0;

		for (Object entry : toMatch) {
			List<Object> mightEntries = mightMatch.toList();
			if( mightEntries.contains(entry)) {
				keysMatched++;
			}
		}
		return (keysMatched == toMatch.length());
	}


	private String addLookup(JSONObject nativeDevice, JSONArray commonDevice) {
		lookupTable.put(nativeDevice,commonDevice);
		return "successful";
	}
}
