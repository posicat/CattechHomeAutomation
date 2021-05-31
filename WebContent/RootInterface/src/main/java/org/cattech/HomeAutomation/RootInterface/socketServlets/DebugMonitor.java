package org.cattech.HomeAutomation.RootInterface.socketServlets;

import java.io.IOException;
import java.util.List;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

public class DebugMonitor extends HomeAutomationModule implements WebSocketListener {

	private Session session;
	private ChannelController controller;

	public DebugMonitor(ChannelController controller) {
		super(controller);
		this.controller = controller;
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		this.session = null;
		HomeAutomationPacket hap = new HomeAutomationPacket("{\"register\":[\"\"],\"nodeName\":\"" + getModuleChannelName() + "\"}");
		hubInterface.sendDataPacketToController(hap);
		this.setRunning(false);
	}

	@Override
	public void onWebSocketConnect(Session session) {
		this.session = session;
		HomeAutomationPacket hap = new HomeAutomationPacket("{\"register\":[\"all\"],\"nodeName\":\"" + getModuleChannelName() + "\"}");
		hubInterface.sendDataPacketToController(hap);
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		// TODO Auto-generated method stub
		System.out.println("onWebSocketError");
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		// TODO Auto-generated method stub
		System.out.println("onWebSocketBinary");

	}

	@Override
	public void onWebSocketText(String message) {
		System.out.println("onWebSocketText : " + message);

		if (!this.isRunning()) {
			this.run(); // Start monitoring on the first received message.
		}

		HomeAutomationPacket hap = new HomeAutomationPacket(message);
		if (hap != null) {
			hubInterface.sendDataPacketToController(hap);
		}
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		try {
			String message = incoming.toString()+"<br>";
			if(null!=session) {
				session.getRemote().sendString(message);
			}
		} catch (IOException e) {
			System.out.println("");
		}
	}

	@Override
	public String getModuleChannelName() {
		return "DebugMonitor";
	}
}
