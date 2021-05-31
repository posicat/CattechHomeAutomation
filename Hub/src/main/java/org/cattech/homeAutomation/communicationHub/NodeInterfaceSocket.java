package org.cattech.homeAutomation.communicationHub;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

public class NodeInterfaceSocket extends NodeInterface {
	private Logger log = LogManager.getLogger(this.getClass());

	protected Socket clientSocket = null;
	private OutputStream output;
	private Scanner input;

	public NodeInterfaceSocket(Socket clientSocket, ChannelController controller) {
		super(controller);
		this.clientSocket = clientSocket;
	}

	@Override
	public void watchForData() {
		try {
			output = clientSocket.getOutputStream();
			input = new Scanner(clientSocket.getInputStream());
		} catch (IOException e) {
			log.error("IOException",e);
		}
		while (super.isRunning()) {
			try {
				super.sendDataPacketToController(new HomeAutomationPacket(input.nextLine()), this);
			} catch (NoSuchElementException e) {
				// Ok if there are no more elements, just return.
				return;
			}
		}
	}

	@Override
	public void sendDataPacketToNode(HomeAutomationPacket hap) throws Exception {
		output.write((hap.toString() + "\r\n").getBytes());
	}
}
