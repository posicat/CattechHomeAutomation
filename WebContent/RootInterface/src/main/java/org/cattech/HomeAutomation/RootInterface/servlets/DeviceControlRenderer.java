package org.cattech.HomeAutomation.RootInterface.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cattech.HomeAutomation.RootInterface.servletBase.ConfiguredServletBase;

public class DeviceControlRenderer  extends ConfiguredServletBase {
	
	private String deviceType;
	private String deviceIndex;
       
    public DeviceControlRenderer() {
        super();
    }

    @Override
	public void setupServletState(HttpServletRequest request, HttpServletResponse response) {
    	super.setupServletState(request, response);
    	
    	deviceType = request.getParameter("type");
    	deviceIndex = request.getParameter("dIdx");
    }

    public String getDeviceType() {
    	return deviceType;
    }
	
	public String generateAction(String action) {
		String script = "processAction('"+deviceIndex+"','"+action+"')";
		return script;
	}
	
	public String getDeviceTitle() {
		return "Device : "+deviceIndex + "(" + deviceType + ")";
	}
	

}
