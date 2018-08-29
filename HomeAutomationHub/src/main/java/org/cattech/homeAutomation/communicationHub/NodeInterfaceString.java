package org.cattech.homeAutomation.communicationHub;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

// Used for loadable module communications and testing.

public class NodeInterfaceString extends NodeInterface {
	private Logger log = Logger.getLogger(this.getClass());

	boolean fullTrace = false;

	private volatile ArrayList<HomeAutomationPacket> dataFromController;

	public NodeInterfaceString(ChannelController controller) {
		super(controller);
		dataFromController = new ArrayList<HomeAutomationPacket>();
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

	@Override
	synchronized public void sendDataPacketToNode(HomeAutomationPacket hap) throws Exception {
		synchronized (dataFromController) {
			if (fullTrace) {
				log.info("<<<FROM CONTROL<<<" + hap.toString());
			}
			dataFromController.add(new HomeAutomationPacket(hap.toString()));
		}
	}

	synchronized public HomeAutomationPacket getDataPacketFromController() {
		synchronized (dataFromController) {
			if (dataFromController.size() > 0) {
				HomeAutomationPacket hap = dataFromController.remove(0);
				return hap;
			} else {
				return null;
			}
		}

	}

	public boolean isFullTrace() {
		return fullTrace;
	}

	public void setFullTrace(boolean fullTrace) {
		this.fullTrace = fullTrace;
	}

	synchronized public void sendDataPacketToController(HomeAutomationPacket hap) {
		if (fullTrace) {
			log.info(">>>TO CONTROL>>>" + hap.toString());
		}
		sendDataPacketToController(hap, this);
	}
}
