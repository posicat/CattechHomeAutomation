package org.cattech.homeAutomation.DeviceEventHandler;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

public class DeviceEventHandler extends HomeAutomationModule {

	public DeviceEventHandler(ChannelController controller) {
		super(controller);
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			String packet = hubInterface.getDataFromController();
			if (packet != null) {
				HomeAutomationPacket hap = new HomeAutomationPacket(this.getModuleChannelName(), packet);
				log.debug("Packet : " + packet);
				if (null != hap.getIn()) {
					if (!hap.getIn().has("data")) {
						log.error("Packet has no data element. " + packet);
					} else {
						if (hap.getDataIn().has("nativeDevice")) {
							hap.setDataOut(hap.getDataIn());
							hap.getDataOut().put("resolution", "toCommon");
							hap.getDataOut().put("postResolv", "WebEventHandler");
							log.info("Sending packet to have nativeDevice decoded.\n" + packet);
							hap.getOut().remove("destination");
							hap.getOut().put("destination",  new String[] {"DeviceResolver"});
						} else {
							hap.setDataOut(hap.getDataIn());
							log.info("Sending packet directly to WebEventHandler.");
							hap.getOut().remove("destination");
							hap.getOut().put("destination", new String[] {"WebEventHandler"});
						}
					}
					if (hap.hasReturnData()) {
						try {
							hubInterface.sendDataToController(hap.getReturnPacket());
							log.debug("Return packet"+hap.getReturnPacket());
						} catch (Exception e) {
							log.error("Error sending message back to node", e);
						}
					}

				} else {
					sleepNoThrow(100);
				}
			}
		}
	}

	@Override
	public String getModuleChannelName() {
		return "DeviceEventHandler";
	}

}
