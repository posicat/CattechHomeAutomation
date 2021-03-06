package org.cattech.homeAutomation.communicationHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.configuration.homeAutomationConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;

//import org.json.JSONArray;
//import org.json.JSONObject;

public class ChannelController {
	private Logger						log			= Logger.getLogger(this.getClass());
	
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
	homeAutomationConfiguration config;

	public homeAutomationConfiguration getConfig() {
		return config;
	}

	public ChannelController(homeAutomationConfiguration config) throws IOException {
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

	public void processIncomingData(String data, NodeInterface fromNode) {
		String errors = "";
		try {

			JSONObject jsonIn = new JSONObject(data);
			JSONObject jsonOut = new JSONObject();

			if (jsonIn.has(NODE_REGISTER_CHANNELS)) {
				if (jsonIn.has(NODE_NODE_NAME)) {
					fromNode.setNodeName(jsonIn.getString(NODE_NODE_NAME));
				} else {
					fromNode.setNodeName(UUID.randomUUID().toString());
				}

				JSONArray registerChannels = jsonIn.getJSONArray(NODE_REGISTER_CHANNELS);
				for (int i = 0; i < registerChannels.length(); i++) {
					addNodeToChannel(registerChannels.getString(i), fromNode);
					// log.info("Registered "+registerChannels.getString(i)+" to
					// "+fromNode);
				}

				jsonOut.put(NODE_STATUS_BLOCK, "registered");
				jsonOut.put(NODE_DATA_SOURCE, NODE_CHANNEL_CONTROLLER);
				jsonOut.put(NODE_DATA_CHANNEL, registerChannels);
				jsonOut.put(NODE_NODE_NAME, fromNode.getNodeName());
				fromNode.sendDataToNode(jsonOut.toString());
			}

			if (jsonIn.has(NODE_DATA_DESTINATION) && jsonIn.has(NODE_DATA_BLOCK)) {
				JSONArray destinations = jsonIn.getJSONArray(NODE_DATA_DESTINATION);
				destinations.put("all"); // Also send to channel all
				JSONObject channelData = jsonIn.getJSONObject(NODE_DATA_BLOCK);
				jsonOut.put(NODE_NODE_NAME, fromNode.getNodeName());
				jsonOut.put(NODE_DATA_SOURCE, jsonIn.get(NODE_DATA_SOURCE));
				jsonOut.put(NODE_DATA_BLOCK, channelData);
				for (int i = 0; i < destinations.length(); i++) {
					String channel = destinations.getString(i);
					jsonOut.put(NODE_DATA_CHANNEL, channel);
					sendToChannel(channel, jsonOut.toString());
				}
			}
		} catch (Exception e) {
			errors += e.getMessage();
		}

		if (errors != "") {
			try {
				JSONObject jsonOut = new JSONObject();
				jsonOut.put(NODE_ERROR_MESSAGE, errors);
				log.error("Error :" + errors);
				log.error(data);
				fromNode.sendDataToNode(jsonOut.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void sendToChannel(String channel, String data) throws Exception {
		ArrayList<NodeInterface> nodes;
		// if (NODE_SEND_TO_ALL_ADDRESS.equals(channel)) {
		// nodes = allNodes;
		// } else {
		nodes = masterChannelList.get(channel);
		// }
		if (nodes != null) {
			for (NodeInterface node : nodes) {
				node.sendDataToNode(data);
			}
		} else {
			if (NODE_SEND_TO_ALL_ADDRESS != channel) {
				log.error("No node registered for channel : " + channel);
				log.error(data);
			}
		}
	}
}
