package org.cattech.homeAutomation.communicationHub;

import java.io.IOException;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

public class NodeInterfaceSocket extends NodeInterface {

	protected Socket clientSocket = null;
	private Scanner input;

	public NodeInterfaceSocket(Socket clientSocket, ChannelController controller) {
		super(controller);
		this.clientSocket = clientSocket;
	}

	@Override
	public void watchForData() {
		try {
			input = new Scanner(clientSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (super.isRunning()) {
			try {
				super.sendDataPacketToController(new HomeAutomationPacket(input.nextLine()), this);
			} catch (NoSuchElementException e) {
				return;
			}
		}
	}

	@Override
	public void sendDataPacketToNode(HomeAutomationPacket hap) throws Exception {
		// TODO Auto-generated method stub

	}
}
