package org.cattech.homeAutomation.moduleBase;

import java.sql.Connection;
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
//		log.info(getModuleChannelName());
		hubInterface.sendDataToController("{\"register\":[\""+getModuleChannelName()+"\"],\"nodeName\":\""+getModuleChannelName()+"\"}");

		String response = null;
		while (null == response) {
			response = hubInterface.getDataFromController();
			sleepNoThrow(100);
		}
		//		log.info(response);
	}

	public String getModuleChannelName() {
		return this.getClass().getSimpleName();
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

	protected Connection getHomeAutomationDBConnection() {
		// TODO Auto-generated method stub
		return null;
	}
}
