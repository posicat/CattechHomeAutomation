package org.cattech.homeAutomation.mqttBroker;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONObject;

import io.moquette.BrokerConstants;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.proto.messages.AbstractMessage.QOSType;
import io.moquette.proto.messages.PublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.MemoryConfig;

public class MQTTBroker extends HomeAutomationModule {
	private static final String PAYLOAD = "payload";
	private static final String MQTT_BROKER = "MQTTBroker";
	static Logger log = LogManager.getLogger(MQTTBroker.class.getName());
	final Server mqttBroker = new Server();

	public MQTTBroker(ChannelController controller) {
		super(controller);

		// controller.getConfig().setLogFileForAppender("MQTTBroker",MQTTBroker.class.getName(),"moquette.log",Level.DEBUG);

		final List<? extends InterceptHandler> userHandlers = Arrays.asList(new PublisherListener(controller));
		try {

			final Properties mqttConfig = new Properties();

			File mapdbPath = new File(controller.getConfig().getConfigFolder() + "/moquette/");
			if (!mapdbPath.exists()) {
				mapdbPath.mkdirs();
			}
			
			mqttConfig.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME,mapdbPath.getCanonicalPath()+"/moquette_store.mapdb");
			mqttBroker.startServer(new MemoryConfig(mqttConfig), userHandlers);

			System.out.println("moquette mqtt broker started, press ctrl-c to shutdown..");
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					System.out.println("stopping moquette mqtt broker..");
					mqttBroker.stopServer();
					System.out.println("moquette mqtt broker stopped");
				}
			});
		} catch (IOException e) {
			log.error("Error starting MQTT Broker", e);
		}
	}

	@Override
	public String getModuleChannelName() {
		return MQTT_BROKER;
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		// {
		// "data":{
		// "native_device":{
		// "topic":"home/commands/IR_GC",
		// "protocol":"MQTT",
		// "controlChannel:"MQTTBroker",
		// "packet":"38000,1,69,341,171,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,64,21,64,21,64,21,64,21,64,21,64,21,64,21,21,21,21,21,64,21,21,21,64,21,21,21,21,21,21,21,21,21,64,21,21,21,64,21,21,21,64,21,64,21,64,21,64,21,1558,341,86,21,3650"
		// },
		// "action":"send"
		// },
		// "channel":"MQTTBroker",
		// "source":"*"
		// }

		if (incoming.getDataString(HomeAutomationPacket.FIELD_DATA_ACTION).equals("send")) {
			JSONObject nativeDevice = incoming.getDataJObj(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE);

			PublishMessage message = new PublishMessage();
			message.setTopicName(nativeDevice.getString("topic"));
			message.setRetainFlag(false);
			message.setQos(QOSType.EXACTLY_ONCE);
			message.setPayload(ByteBuffer.wrap(nativeDevice.getString(PAYLOAD).getBytes()));

			mqttBroker.internalPublish(message);
		}
	}

	class PublisherListener extends AbstractInterceptHandler {
		private static final String RECEIVED = "receive";
		private static final String MQTT = "MQTT";
		private static final String PROTOCOL = "protocol";
		private static final String TOPIC = "topic";
		private static final String CLIENT_ID = "clientID";
		private NodeInterfaceString hubInterface;

		public PublisherListener(ChannelController controller) {
			this.hubInterface = new NodeInterfaceString(controller);
		}

		@Override
		public void onPublish(InterceptPublishMessage message) {
			String payload = new String(message.getPayload().array());
			System.out.println("moquette mqtt broker message intercepted, topic: " + message.getTopicName() + ", content: " + payload);

			HomeAutomationPacket hap = new HomeAutomationPacket();
			hap.setSource(MQTT_BROKER);
			hap.setDestination(HomeAutomationPacket.CHANNEL_EVENT_HANDLER);
			JSONObject nativeDevice = new JSONObject();
			nativeDevice.put(PROTOCOL, MQTT);
			nativeDevice.put(TOPIC, message.getTopicName());
			nativeDevice.put(CLIENT_ID, message.getClientID());
			nativeDevice.put(PAYLOAD, payload);
			hap.putData(HomeAutomationPacket.FIELD_DATA_ACTION, RECEIVED);
			hap.putData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE, nativeDevice);
			hap.putData(HomeAutomationPacket.FIELD_RESOLUTION, HomeAutomationPacket.RESOLUTION_TO_COMMON);

			hubInterface.sendDataPacketToController(hap);
		}
	}
}
