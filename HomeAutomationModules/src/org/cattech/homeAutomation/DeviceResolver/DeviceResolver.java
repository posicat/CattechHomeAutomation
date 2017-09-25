package org.cattech.homeAutomation.DeviceResolver;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;

public class DeviceResolver extends HomeAutomationModule {
	public DeviceResolver(ChannelController controller) {
			super(controller);
		}

	@Override
	public void run() {
		running=true;
		while (running) {
			sleepNoThrow(10000);
		}
	}

}
