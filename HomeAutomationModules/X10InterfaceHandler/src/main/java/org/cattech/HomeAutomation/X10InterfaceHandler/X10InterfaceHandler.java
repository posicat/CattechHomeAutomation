package org.cattech.HomeAutomation.X10InterfaceHandler;

import java.io.IOException;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONObject;

public class X10InterfaceHandler extends HomeAutomationModule {

	public X10InterfaceHandler(ChannelController controller) {
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
				if (! hap.getIn().getString("channel").equalsIgnoreCase("x10Controller")) {
					log.error("Packet is not for this listener. " + packet);
					return;
				}
					
				if (null != hap.getIn()) {
					if (!hap.getIn().has("data")) {
						log.error("Packet has no data element. " + packet);
					} else {
						JSONObject device = hap.getDataIn().getJSONObject("nativeDevice");
						String unit = device.getString("unit");
						String house = device.getString("house");
						String action=hap.getDataIn().getString("action");
						
						String heyuCommand = "/usr/local/bin/heyu turn "+house.toLowerCase()+unit+" "+action.toLowerCase();
						log.info("Executing : "+heyuCommand);
						Runtime rt = Runtime.getRuntime();
						Process pr;
						int retVal = 0;
						try {
							pr = rt.exec(heyuCommand);
							retVal = pr.waitFor();
						} catch (IOException | InterruptedException e) {
							log.error("Command failed to execute",e);
						}
						if (retVal != 0) {
							log.error("Command exited with :"+retVal);
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
	}

	@Override
	public String getModuleChannelName() {
		return "x10Controller";
	}

}
