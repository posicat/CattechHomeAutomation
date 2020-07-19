package org.cattech.HomeAutomation.IkeaTradfriInterfaceHandler;

import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONObject;

public class TradfriInterfaceHandler extends HomeAutomationModule {
	static Logger log = Logger.getLogger(TradfriInterfaceHandler.class.getName());

	public TradfriInterfaceHandler(ChannelController controller) {
		super(controller);
		log.info("Post Super");

		starttradfriMonitor();
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

	private void starttradfriMonitor() {
		log.info("Starting Heyu wrapper");
		
		
		log.info("Started Heyu wrapper");
	}
}
