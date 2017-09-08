package org.cattech.homeAutomation.communicationHub;

import org.cattech.homeAutomation.channelHandlers.EventHandlerWebRelay;

public class Hub {

	private static ChannelController controller = new ChannelController();
	
	public static void main(String[] args) {
		NodeSocketConnectionManager server = new NodeSocketConnectionManager(10042,controller);
		new Thread(server,"Connection Manager").start();
		
		EventHandlerWebRelay eventHandler = new EventHandlerWebRelay(controller,"http://pawz.cattech.org/pw/homeAutomation/eventHandler.cgi?event=");
		new Thread(eventHandler,"Event Handler Relay").start();
		
		System.out.println("Listening...");
		try {
			while (!server.isStopped()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Stopping Server");
		server.stop();
	}

}
