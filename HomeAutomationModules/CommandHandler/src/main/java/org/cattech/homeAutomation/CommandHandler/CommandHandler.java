package org.cattech.homeAutomation.CommandHandler;

import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacketHelper;

public class CommandHandler extends HomeAutomationModule {
	static Logger log = Logger.getLogger(CommandHandler.class.getName());

	public CommandHandler(ChannelController controller) {
		super(controller);
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (incoming.hasWrapper()) {
			if (!incoming.hasData()) {
				log.error("Packet has no data element. " + incoming);
			} else {
				HomeAutomationPacket reply =HomeAutomationPacketHelper.generateReplyPacket(incoming, getModuleChannelName());
				reply.setData(incoming.getData());
				reply.putData("resolution", "toNative");
				reply.removeFromWrapper("destination");
				reply.putWrapper("destination", new String[] { "DeviceResolver" });
				outgoing.add(reply);
			}
		}
	}

	@Override
	public String getModuleChannelName() {
		return "DeviceCommandHandler";
	}
}
