package org.cattech.homeAutomation.communicationHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChannelController {
	private Logger log = Logger.getLogger(this.getClass());

	Hashtable<String, ArrayList<NodeInterface>> masterChannelList = new Hashtable<String, ArrayList<NodeInterface>>();
	ArrayList<NodeInterface> masterNodeList = new ArrayList<NodeInterface>();
	HomeAutomationConfiguration config;

	public HomeAutomationConfiguration getConfig() {
		return config;
	}

	public ChannelController(HomeAutomationConfiguration config) throws IOException {
		this.config = config;
	}

	public void addNodeToChannel(String channel, NodeInterface node) {
		ArrayList<NodeInterface> nodes = masterChannelList.get(channel);
		nodes = addNodeToList(nodes, node);
		masterChannelList.put(channel, nodes);
	}

	public void removeNodeFromChannel(String channel, NodeInterface node) {
		ArrayList<NodeInterface> nodes = masterChannelList.get(channel);
		removeNodeFromList(nodes, node);
		masterChannelList.put(channel, nodes);
	}

	public void addNode(NodeInterface node) {
		addNodeToList(masterNodeList, node);
	}

	public void removeNode(NodeInterface node) {
		removeNodeFromList(masterNodeList, node);
	}

	private static ArrayList<NodeInterface> removeNodeFromList(ArrayList<NodeInterface> nodes, NodeInterface node) {
		if (nodes == null) {
			nodes = new ArrayList<NodeInterface>();
		}
		nodes.remove(node);
		return nodes;
	}

	private static ArrayList<NodeInterface> addNodeToList(ArrayList<NodeInterface> nodes, NodeInterface node) {
		if (nodes == null) {
			nodes = new ArrayList<NodeInterface>();
		}
		if (!nodes.contains(node)) {
			nodes.add(node);
		}
		return nodes;
	}


	public void processIncomingDataPacket(HomeAutomationPacket hapIn, NodeInterface fromNode) {
		String errors = "";
		try {
			log.debug("<--- FROM " + fromNode.getNodeName() + " " + hapIn.toString());

			HomeAutomationPacket hapOut = new HomeAutomationPacket();

			if (hapIn.hasWrapper(HomeAutomationPacket.FIELD_REGISTER)) {
				if (hapIn.hasWrapper(HomeAutomationPacket.FIELD_NODE_NAME)) {
					fromNode.setNodeName(hapIn.getWrapperString(HomeAutomationPacket.FIELD_NODE_NAME));
				} else {
					fromNode.setNodeName(UUID.randomUUID().toString());
				}

				JSONArray registerChannels = hapIn.getWrapperJArr(HomeAutomationPacket.FIELD_REGISTER);
				for (int i = 0; i < registerChannels.length(); i++) {
					addNodeToChannel(registerChannels.getString(i), fromNode);
					// log.info("Registered "+registerChannels.getString(i)+" to
					// "+fromNode);
				}

				hapOut.putWrapper(HomeAutomationPacket.FIELD_STATUS, "registered");
				hapOut.putWrapper(HomeAutomationPacket.FIELD_SOURCE, HomeAutomationPacket.CHANNEL_CONTROLLER);
				hapOut.putWrapper(HomeAutomationPacket.FIELD_CHANNEL, registerChannels);
				hapOut.putWrapper(HomeAutomationPacket.FIELD_NODE_NAME , fromNode.getNodeName());
				fromNode.sendDataPacketToNode(hapOut);
				log.debug("---> TO " + registerChannels + " " + hapOut);

			}

			if (hapIn.hasWrapper(HomeAutomationPacket.FIELD_DESTINATION) && hapIn.hasData()) {
				JSONObject channelData = hapIn.getData();
				hapOut.putWrapper(HomeAutomationPacket.FIELD_NODE_NAME, fromNode.getNodeName());
				hapOut.putWrapper(HomeAutomationPacket.FIELD_SOURCE, hapIn.getWrapperString(HomeAutomationPacket.FIELD_SOURCE));
				hapOut.setData(channelData);
				
				JSONArray destinations = hapIn.getWrapperJArr(HomeAutomationPacket.FIELD_DESTINATION);
				for (int i = 0; i < destinations.length(); i++) {
					String channel = destinations.getString(i);
					hapOut.putWrapper(HomeAutomationPacket.FIELD_CHANNEL, channel);
					sendToChannel(channel, hapOut, true);
					log.debug("---> TO " + channel + " " + hapOut);
				}
				hapOut.removeFromWrapper(HomeAutomationPacket.FIELD_CHANNEL);
				hapOut.putWrapper(HomeAutomationPacket.FIELD_ALL_CHANNELS, destinations.toString());
				sendToChannel("all", hapOut, false);
			}
		} catch (Exception e) {
			log.debug("Error processing packet : "+hapIn,e);
			errors += e.getMessage();
		}

		if (errors != "") {
			try {
				HomeAutomationPacket hapOut = new HomeAutomationPacket();
				hapOut.setDestination(hapIn.getWrapperString(HomeAutomationPacket.FIELD_SOURCE));
				hapOut.putWrapper(HomeAutomationPacket.FIELD_ERROR, errors);
				hapOut.putWrapper(HomeAutomationPacket.FIELD_SOURCE, HomeAutomationPacket.CHANNEL_CONTROLLER);
				log.error("Error :" + errors);
				log.error("IN:"+hapIn);
				log.error("OUT:"+hapOut);
				fromNode.sendDataPacketToNode(hapOut);
			} catch (Exception e) {
				log.error("Could not build output packet",e);
			}
		}
	}

	private void sendToChannel(String channel, HomeAutomationPacket hapOut, boolean throwNoChannel) throws Exception {
		ArrayList<NodeInterface> nodes;
		// if (HomeAutomationPacket.SEND_TO_ALL_ADDRESS.equals(channel)) {
		// nodes = allNodes;
		// } else {
		nodes = masterChannelList.get(channel);
		// }
		if (nodes != null) {
			for (NodeInterface node : nodes) {
				node.sendDataPacketToNode(hapOut);
			}
		} else {
			if (throwNoChannel) {
				log.error("No node registered for channel : " + channel);
				log.error(hapOut);
			}
		}
	}
}
