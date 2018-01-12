package org.cattech.homeAutomation.communicationHub;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

public class NodeInterfaceSocket extends NodeInterface {

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
			e.printStackTrace();
		}
		while (super.isRunning()) {
			try {
				super.sendDataPacketToController(new HomeAutomationPacket(this.getNodeName(),input.nextLine()), this);
			} catch (NoSuchElementException e) {
				return;
			}
		}
	}
	
	/**
	 * @deprecated This method is replaced by sendDataPacketToNode 
	 * 
	 */
	@Deprecated
	@Override
	public void sendDataToNode(String data) throws IOException {
		output.write((data + "\r\n").getBytes());
	}

	@Override
	public void sendDataPacketToNode(HomeAutomationPacket hap) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
