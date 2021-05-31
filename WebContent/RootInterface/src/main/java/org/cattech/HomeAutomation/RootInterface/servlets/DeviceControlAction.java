package org.cattech.HomeAutomation.RootInterface.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.HomeAutomation.RootInterface.servletBase.ConfiguredServletBase;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONArray;
import org.json.JSONObject;

public class DeviceControlAction  extends ConfiguredServletBase {
	static Logger log = LogManager.getLogger(DeviceControlAction.class.getName());

	
	private int deviceID;
	private String action;
	private String status = "";
       
    public DeviceControlAction() {
        super();
    }

    @Override
	public String getModuleChannelName() {
		return "DeviceControlAction";
	}
    
    @Override
	public void setupServletState(HttpServletRequest request, HttpServletResponse response) {
    	super.setupServletState(request, response);
    	
    	deviceID = Integer.parseInt(request.getParameter("devID"));
    	action = request.getParameter("action");
    }

    public void processDeviceAction() {
    	JSONObject deviceTarget = loadDevice(deviceID);
    	
    	
    	//{"interfaceType":"lamp_dimbri","nativeDevice":"{\"house\":\"B\",\"unit\":\"14\",\"protocol\":\"x10\",\"controlChannel\":\"x10Controller\"}"}
    	
    	HomeAutomationPacket hap = new HomeAutomationPacket();
    	
    	if (deviceTarget.has(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE)) {
    		JSONObject nativeDevice = deviceTarget.getJSONObject(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE);
    		hap.setSource(getModuleChannelName());
    		hap.setDestination(nativeDevice.getString(HomeAutomationPacket.FIELD_NATIVE_CONTROL_CHANNEL));
    		hap.getData().put(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE,nativeDevice);
    		
    		JSONArray actions = new JSONArray();
    		actions.put(action);

    		hap.getData().put(HomeAutomationPacket.FIELD_DATA_ACTIONS, actions);
    	}
//    	JSONObject device = deviceTarget.getJSONObject(HomeAutomationPacket.FIELD_DATA_DEVICE);

    	log.debug("SENDING PACKET : "+hap.toString());
    	hubInterface.sendDataPacketToController(hap);
    }
    
	public String displayStatus() {
    	return status;
    }
}
