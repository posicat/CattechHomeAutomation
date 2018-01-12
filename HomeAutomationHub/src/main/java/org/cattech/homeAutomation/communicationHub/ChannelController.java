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

//import org.json.JSONArray;
//import org.json.JSONObject;

public class ChannelController {
	private Logger log = Logger.getLogger(this.getClass());

	// Data packet field names
	public static final String NODE_DATA_SOURCE = "source";
	public static final String NODE_DATA_DESTINATION = "destination";
	public static final String NODE_DATA_BLOCK = "data";
	public static final String NODE_REGISTER_CHANNELS = "register";
	public static final String NODE_STATUS_BLOCK = "status";
	public static final String NODE_ERROR_MESSAGE = "error";
	public static final String NODE_NODE_NAME = "nodeName";
	private static final String NODE_DATA_CHANNEL = "channel";

	// Channel constants
	public static final String NODE_CHANNEL_CONTROLLER = "ChannelController";
	public static final String NODE_SEND_TO_ALL_ADDRESS = "all";

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


	/**
	 * @deprecated This method will be replaced by sendDataPacketToController 
	 * 
	 */
	@Deprecated	
	public void processIncomingData(String incoming, NodeInterface fromNode) {
		processIncomingDataPacket(new HomeAutomationPacket(fromNode.getNodeName(),incoming),fromNode);
	}
		
	public void processIncomingDataPacket(HomeAutomationPacket hapIn, NodeInterface fromNode) {
		String errors = "";
		try {
			log.debug("<--- FROM " + fromNode.getNodeName() + " " + hapIn.toString());

			HomeAutomationPacket hapOut = new HomeAutomationPacket();

			if (hapIn.getWrapper().has(NODE_REGISTER_CHANNELS)) {
				if (hapIn.getWrapper().has(NODE_NODE_NAME)) {
					fromNode.setNodeName(hapIn.getWrapper().getString(NODE_NODE_NAME));
				} else {
					fromNode.setNodeName(UUID.randomUUID().toString());
				}

				JSONArray registerChannels = hapIn.getWrapper().getJSONArray(NODE_REGISTER_CHANNELS);
				for (int i = 0; i < registerChannels.length(); i++) {
					addNodeToChannel(registerChannels.getString(i), fromNode);
					// log.info("Registered "+registerChannels.getString(i)+" to
					// "+fromNode);
				}

				hapOut.getWrapper().put(NODE_STATUS_BLOCK, "registered");
				hapOut.getWrapper().put(NODE_DATA_SOURCE, NODE_CHANNEL_CONTROLLER);
				hapOut.getWrapper().put(NODE_DATA_CHANNEL, registerChannels);
				hapOut.getWrapper().put(NODE_NODE_NAME, fromNode.getNodeName());
				fromNode.sendDataPacketToNode(hapOut);
				log.debug("---> TO " + registerChannels + " " + hapOut.getWrapper());

			}

			if (hapIn.getWrapper().has(NODE_DATA_DESTINATION) && hapIn.getWrapper().has(NODE_DATA_BLOCK)) {
				JSONObject channelData = hapIn.getWrapper().getJSONObject(NODE_DATA_BLOCK);
				hapOut.getWrapper().put(NODE_NODE_NAME, fromNode.getNodeName());
				hapOut.getWrapper().put(NODE_DATA_SOURCE, hapIn.getWrapper().get(NODE_DATA_SOURCE));
				hapOut.setData(channelData);

				JSONArray destinations = hapIn.getWrapper().getJSONArray(NODE_DATA_DESTINATION);
				for (int i = 0; i < destinations.length(); i++) {
					String channel = destinations.getString(i);
					hapOut.getWrapper().put(NODE_DATA_CHANNEL, channel);
					sendToChannel(channel, hapOut, true);
					log.debug("---> TO " + channel + " " + hapOut.getWrapper());
				}
				hapOut.getWrapper().put("all_channels", destinations.toString());
				sendToChannel("all", hapOut, false);
			}
		} catch (Exception e) {
			errors += e.getMessage();
		}

		if (errors != "") {
			try {
				HomeAutomationPacket hapOut = new HomeAutomationPacket();
				hapOut.getWrapper().put(NODE_ERROR_MESSAGE, errors);
				log.error("Error :" + errors);
				log.error(hapIn);
				fromNode.sendDataPacketToNode(hapOut);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void sendToChannel(String channel, HomeAutomationPacket hapOut, boolean throwNoChannel) throws Exception {
		ArrayList<NodeInterface> nodes;
		// if (NODE_SEND_TO_ALL_ADDRESS.equals(channel)) {
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
