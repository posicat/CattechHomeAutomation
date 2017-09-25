package DeviceResolver;

import java.util.Hashtable;

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
		}
	
		if (hap.hasReturnData()) {
			hubInterface.sendDataToController(hap.getReturnPacket());
		}
	}

	private String addLookup(JSONObject nativeDevice, JSONArray commonDevice) {
		lookupTable.put(nativeDevice,commonDevice);
		return "successful";
	}
}
