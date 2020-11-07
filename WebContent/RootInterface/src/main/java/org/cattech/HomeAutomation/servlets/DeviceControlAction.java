package org.cattech.HomeAutomation.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cattech.HomeAutomation.servletBase.ConfiguredServletBase;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONObject;

public class DeviceControlAction  extends ConfiguredServletBase {
	
	private int deviceID;
	private String action;
	private String status = "";
       
    public DeviceControlAction() {
        super();
    }

    @Override
	public void setupServletState(HttpServletRequest request, HttpServletResponse response) {
    	super.setupServletState(request, response);
    	
    	deviceID = Integer.parseInt(request.getParameter("devID"));
    	action = request.getParameter("action");
    }

    public void processDeviceAction() {
    	JSONObject device = loadDevice(deviceID);
    	
    	HomeAutomationPacket hap = new HomeAutomationPacket();
    }
    
	public String displayStatus() {
    	return status;
    }
}
