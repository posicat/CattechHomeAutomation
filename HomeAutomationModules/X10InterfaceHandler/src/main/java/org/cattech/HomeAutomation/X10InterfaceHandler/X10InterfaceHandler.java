package org.cattech.HomeAutomation.X10InterfaceHandler;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONObject;

public class X10InterfaceHandler extends HomeAutomationModule {
	static Logger log = Logger.getLogger(X10InterfaceHandler.class.getName());

	public X10InterfaceHandler(ChannelController controller) {
		super(controller);
	}

	@Override
	public String getModuleChannelName() {
		return "x10Controller";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (!incoming.hasWrapper("data")) {
			log.error("Packet has no data element. " + incoming);
		} else {
			JSONObject device = incoming.getDataJObj(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE);
			String unit = device.getString("unit");
			String house = device.getString("house");
			String action = incoming.getDataString("action");

			String heyuCommand = "/usr/local/bin/heyu turn " + house.toLowerCase() + unit + " " + action.toLowerCase();
			log.info("Executing : " + heyuCommand);
			Runtime rt = Runtime.getRuntime();
			Process pr;
			int retVal = 0;
			try {
				pr = rt.exec(heyuCommand);
				retVal = pr.waitFor();
				pr.destroy();
			} catch (IOException | InterruptedException e) {
				log.error("Command failed to execute", e);
			}
			if (retVal != 0) {
				log.error("Command exited with :" + retVal);
			}
		}
	}
}
