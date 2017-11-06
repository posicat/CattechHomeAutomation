package org.cattech.homeAutomation.DeviceEventHandler;

import java.util.List;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacketHelper;

public class DeviceEventHandler extends HomeAutomationModule {


	public DeviceEventHandler(ChannelController controller) {
		super(controller);
	}

	@Override
	public String getModuleChannelName() {
		return "DeviceEventHandler";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		log.debug("Packet : " + incoming);
		if (null != incoming.getWrapper()) {
			if (!incoming.getWrapper().has("data")) {
				log.error("Packet has no data element. " + incoming);
			} else {
				HomeAutomationPacket reply =HomeAutomationPacketHelper.generateReplyPacket(incoming, getModuleChannelName());
				if (incoming.getData().has("nativeDevice")) {
					reply.setData(incoming.getData());
					reply.getData().put("resolution", "toCommon");
					reply.getData().put("postResolv", "EventHandler");
					log.info("Sending packet to have nativeDevice decoded.\n" + incoming);
					reply.getWrapper().remove("destination");
					reply.getWrapper().put("destination", new String[] { "DeviceResolver" });
				} else {
					reply.setData(incoming.getData());
					log.info("Sending packet directly to EventHandler.");
					reply.getWrapper().remove("destination");
					reply.getWrapper().put("destination", new String[] { "EventHandler" });
				}
				outgoing.add(reply);
			}
		}
	}

}
