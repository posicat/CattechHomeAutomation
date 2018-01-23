package org.cattech.homeAutomation.communicationHub;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

// Used for loadable module communications and testing.

public class NodeInterfaceString extends NodeInterface {
	private Logger log = Logger.getLogger(this.getClass());

	boolean fullTrace = false;

	private volatile ArrayList<HomeAutomationPacket> dataFromController;

	private String channel;

	public NodeInterfaceString(ChannelController controller) {
		super(controller);
		this.dataFromController = new ArrayList<HomeAutomationPacket>();
		dataFromController = new ArrayList<HomeAutomationPacket>();
	}

	public NodeInterfaceString(ChannelController controller, String channel) {
		this(controller);
		this.channel=channel;
	}

	// This method never really needs to be called, since we don't need to watch for
	// a string to be sent
	// but it's here just in case some other method relies on it doing something.
	@Override
	public void watchForData() {
		while (super.isRunning()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// OK to ignore, exiting.
			}
		}
	}
	/**
	 *  @deprecated This method is replaced by getDataPacketFromController
	 * 
	 */
	@Deprecated
	@Override
	synchronized public void sendDataToNode(String data) throws Exception {
		if (fullTrace) {
			log.info("<<<FROM CONTROL<<<" + data);
		}
		dataFromController.add(new HomeAutomationPacket(data));
	}

	@Override
	synchronized public void sendDataPacketToNode(HomeAutomationPacket hap) throws Exception {
		if (fullTrace) {
			log.info("<<<FROM CONTROL<<<" + hap.toString());
		}
		dataFromController.add(new HomeAutomationPacket(hap.toString()));
	}

	/**
	 *  @deprecated This method is replaced by getDataPacketFromController
	 * 
	 */
	@Deprecated
	synchronized public String getDataFromController() {
		if (dataFromController.size() > 0) {
			return dataFromController.remove(0).toString();
		} else {
			return null;
		}
	}

	synchronized public HomeAutomationPacket getDataPacketFromController() {
		if (dataFromController.size() > 0) {
			HomeAutomationPacket hap = dataFromController.remove(0);
			return hap;
		} else {
			return null;
		}
	}

	private String getModuleChannelName() {
		return channel;
	}
	
	private void setModuleChannelName(String moduleChannelName) {
		this.channel=moduleChannelName;
	}

	public boolean isFullTrace() {
		return fullTrace;
	}

	public void setFullTrace(boolean fullTrace) {
		this.fullTrace = fullTrace;
	}

	/**
	 * @deprecated This method is replaced by sendDataPacketToController 
	 * 
	 */
	@Deprecated
	synchronized public void sendDataToController(String data) {
		if (fullTrace) {
			log.info(">>>TO CONTROL>>>" + data);
		}
		sendDataToController(data, this);
	}
	synchronized public void sendDataPacketToController(HomeAutomationPacket hap) {
		if (fullTrace) {
			log.info(">>>TO CONTROL>>>" + hap.toString() );
		}
		sendDataPacketToController(hap, this);
		
	}
}
