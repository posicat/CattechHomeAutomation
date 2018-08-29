package org.cattech.homeAutomation.communicationHub;

import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

public abstract class NodeInterface implements Runnable {

	private boolean running = false;
	protected ChannelController controller = null;
	protected String nodeName;

	public NodeInterface(ChannelController controller) {
		super();
		this.controller = controller;
		this.controller.addNode(this);
	}

	public void shutdown() {
		this.controller.removeNode(this);
	}

	@Override
	public void run() {
		this.running = true;
		watchForData();
		this.running = false;
		shutdown();
	}

	public void stop() {
		this.running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public void sendDataPacketToController(HomeAutomationPacket hap, NodeInterface fromNode) {
		this.controller.processIncomingDataPacket(hap, fromNode);
	}

	// Abstract Methods
	public abstract void watchForData();

	public abstract void sendDataPacketToNode(HomeAutomationPacket hap) throws Exception;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public String toString() {
		return("Node : " + this.getNodeName());
	}

}