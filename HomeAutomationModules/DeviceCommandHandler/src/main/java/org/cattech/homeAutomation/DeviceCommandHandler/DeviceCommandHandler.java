package org.cattech.homeAutomation.DeviceCommandHandler;

import java.util.List;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacketHelper;

public class DeviceCommandHandler extends HomeAutomationModule {

	public DeviceCommandHandler(ChannelController controller) {
		super(controller);
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		log.debug("Packet : " + incoming);
		if (null != incoming.getWrapper()) {
			if (!incoming.getWrapper().has("data")) {
				log.error("Packet has no data element. " + incoming);
			} else {
				HomeAutomationPacket reply =HomeAutomationPacketHelper.generateReplyPacket(incoming, getModuleChannelName());
				reply.setData(incoming.getData());
				reply.getData().put("resolution", "toNative");
				reply.getWrapper().remove("destination");
				reply.getWrapper().put("destination", new String[] { "DeviceResolver" });
				outgoing.add(reply);
			}
		}
	}

	@Override
	public String getModuleChannelName() {
		return "DeviceCommandHandler";
	}
}
