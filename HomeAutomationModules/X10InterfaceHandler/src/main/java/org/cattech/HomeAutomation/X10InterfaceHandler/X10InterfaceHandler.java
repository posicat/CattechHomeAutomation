package org.cattech.HomeAutomation.X10InterfaceHandler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.HeyuWrapper.HeyuWrapper;
import org.cattech.HeyuWrapper.HeyuWrapperCallback;
import org.cattech.HeyuWrapper.HeyuWrapperException;
import org.cattech.HeyuWrapper.X10Action;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONException;
import org.json.JSONObject;

public class X10InterfaceHandler extends HomeAutomationModule {
	static Logger log = Logger.getLogger(X10InterfaceHandler.class.getName());

	X10InterfaceHandler(ChannelController controller) {
		super(controller);
		log.info("Post Super");

		startX10Monitor();
	}

	@Override
	public String getModuleChannelName() {
		return "x10Controller";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (!incoming.hasWrapper("data")) {
			log.error("Packet has no data element. " + incoming);
		} else {
			JSONObject device = incoming.getDataJObj(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE);

			X10Action sendX10 = new X10Action();
			try {
				sendX10.setUnit(Integer.parseInt(device.getString("unit")));
				sendX10.setHousecode(device.getString("house").charAt(0));
				sendX10.setAction(device.getString(HomeAutomationPacket.FIELD_DATA_ACTION));

				HeyuWrapper.executeX10HeyuAction(sendX10);
			} catch (NumberFormatException | JSONException | HeyuWrapperException e) {
				log.error("Something went wrong sending X10 command", e);
			}
		}
	}

	private void startX10Monitor() {
		log.info("Starting Heyu wrapper");
		
		Thread wrapperThread = new Thread(new WrapperCallback(this));
		wrapperThread.start();
		
		log.info("Started Heyu wrapper");
	}

}

class WrapperCallback implements HeyuWrapperCallback, Runnable {

	private X10InterfaceHandler x10InterfaceHandler;

	public WrapperCallback(X10InterfaceHandler x10InterfaceHandler) {
		this.x10InterfaceHandler = x10InterfaceHandler;
	}

	@Override
	public void heyuEventReceiver(X10Action receiveEvent) {
		Logger log = Logger.getLogger(X10InterfaceHandler.class.getName() + ":" + WrapperCallback.class.getName());
		
		List<HomeAutomationPacket> outgoing = new ArrayList<HomeAutomationPacket>();

		log.info("Received packet from heyu : "+ receiveEvent.toString());
		
		HomeAutomationPacket hap = new HomeAutomationPacket();
		hap.setSource(x10InterfaceHandler.getModuleChannelName());
		hap.setDestination(HomeAutomationPacket.CHANNEL_EVENT_HANDLER);
		JSONObject nativeDevice = new JSONObject();
		nativeDevice.put("protocol", "x10");
		nativeDevice.put("unit", receiveEvent.getUnit());
		nativeDevice.put("house", receiveEvent.getHousecode());
		nativeDevice.put("delta", receiveEvent.getScaledDelta());
		nativeDevice.put("x10_source", receiveEvent.getSource());
		hap.putData(HomeAutomationPacket.FIELD_DATA_ACTION, receiveEvent.getAction());
		hap.putData(HomeAutomationPacket.FIELD_DATA_DELTA, receiveEvent.getPercentDelta());
		hap.putData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE, nativeDevice);
		hap.putData(HomeAutomationPacket.FIELD_RESOLUTION, HomeAutomationPacket.RESOLUTION_TO_COMMON);

		outgoing.add(hap);

		log.info("Sending X10 message to eventHandler : " + hap.toString());
		
		x10InterfaceHandler.processOutgoingPackets(outgoing);
	}

	@Override
	public void run() {
		HeyuWrapper.registerListener(this);
	};
}
