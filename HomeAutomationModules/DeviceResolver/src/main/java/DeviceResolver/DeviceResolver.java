package DeviceResolver;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import communicationHub.ChannelController;
import moduleBase.HomeAutomationModule;

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
					cDevs = convertToCommonNames(hap.getDataIn().getJSONObject("nativeDevice"));
					hap.setDataOut(hap.getDataIn());
					hap.getDataOut().remove("nativeDevice");
					for (JSONArray cDev : cDevs) {
						hap.getDataOut().remove("device");
						hap.getDataOut().put("device",cDev);
						hubInterface.sendDataToController(hap.getReturnPacket());
					}
				}
			}
			if ("toNative".equals(resolution)) {
				convertToNativeName(hap);
			}
		}
	
		if (hap.hasReturnData()) {
			hubInterface.sendDataToController(hap.getReturnPacket());
		}
	}

	private void convertToNativeName(HomeAutomationPacket hap) {
		// TODO Auto-generated method stub
		
	}

	private Set<JSONArray> convertToCommonNames(JSONObject nativeDevice) {
		Set<JSONArray> resultDevices= new HashSet<JSONArray>();
		
		for (JSONObject nDev : lookupTable.keySet()) {
			if(allKeysMatch(nDev,nativeDevice)){
				JSONArray cDev = lookupTable.get(nDev);
				resultDevices.add(cDev);
			}
		}
		return resultDevices;
	}

	private boolean allKeysMatch(JSONObject mightMatch, JSONObject toMatch) {
		int keysMatched = 0;
		for (String key : toMatch.keySet()) {
			if( mightMatch.has(key) && mightMatch.get(key).equals(toMatch.get(key)) ) {
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
