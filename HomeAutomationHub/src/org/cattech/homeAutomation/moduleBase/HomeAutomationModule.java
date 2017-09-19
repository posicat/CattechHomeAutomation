package org.cattech.homeAutomation.moduleBase;

import java.util.logging.Logger;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;

public abstract class HomeAutomationModule implements Runnable {
	protected NodeInterfaceString hubInterface;
	protected boolean running=false;
	protected Logger log = null;

	protected HomeAutomationModule(ChannelController controller) {
		log = Logger.getLogger( this.getClass().getSimpleName() );
		
		this.hubInterface = new NodeInterfaceString(controller);
		System.out.println(getModuleChannelName());
		hubInterface.sendDataToController("{\"register\":[\""+getModuleChannelName()+"\"]}");

		String response = null;
		while (null == response) {
			response = hubInterface.getDataFromController();
			sleepNoThrow(100);
		}
		//		System.out.println(response);
	}

	public String getModuleChannelName() {
		return "eventHandler";
	}

	public void sleepNoThrow(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e1) {
			// We don't care, just go on.
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
