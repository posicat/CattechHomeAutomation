package org.cattech.homeAutomation.communicationHub;

import java.util.ArrayList;

import org.apache.log4j.Logger;

// Mainly for testing this implements a string based node controller.

public class NodeInterfaceString extends NodeInterface {
	private Logger						log			= Logger.getLogger(this.getClass());

	boolean								fullTrace	= false;

	private volatile ArrayList<String>	dataFromController;

	public NodeInterfaceString(ChannelController controller) {
		super(controller);
		this.dataFromController = new ArrayList<String>();
		dataFromController = new ArrayList<String>();
	}

	// This method never really needs to be called, since we don't need to watch for a string to be sent
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
	synchronized public void sendDataToNode(String data) throws Exception {
		if (fullTrace) {
			log.info("<<<FROM CONTROL<<<" + data);
		}
		dataFromController.add(data);
	}

	synchronized public String getDataFromController() {
		if (dataFromController.size() > 0) {
			String data = dataFromController.remove(0);
			return data;
		} else {
			return null;
		}
	}

	synchronized public void sendDataToController(String data) {
		if (fullTrace) {
			log.info(">>>TO CONTROL>>>" + data);
		}
		sendDataToController(data, this);
	}

	public boolean isFullTrace() {
		return fullTrace;
	}

	public void setFullTrace(boolean fullTrace) {
		this.fullTrace = fullTrace;
	}
}
