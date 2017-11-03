package org.cattech.homeAutomation.DeviceCommandHandler;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

public class DeviceCommandHandler extends HomeAutomationModule {

	public DeviceCommandHandler(ChannelController controller) {
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
						hap.setDataOut(hap.getDataIn());
						hap.getDataOut().put("resolution", "toNative");
						hap.getOut().remove("destination");
						hap.getOut().put("destination", new String[] { "DeviceResolver" });
					}
				}
				if (hap.hasReturnData()) {
					try {
						hubInterface.sendDataToController(hap.getReturnPacket());
						log.debug("Return packet" + hap.getReturnPacket());
					} catch (Exception e) {
						log.error("Error sending message back to node", e);
					}
				}

			} else {
				sleepNoThrow(100);
			}
		}
	}

	@Override
	public String getModuleChannelName() {
		return "DeviceCommandHandler";
	}

	@Override
	protected void processMessage(HomeAutomationPacket hap) {
		// TODO Auto-generated method stub
		
	}

}
