package DeviceResolver;

import communicationHub.ChannelController;
import moduleBase.HomeAutomationModule;

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
