package org.cattech.homeAutomation.mqttBroker;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONObject;

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.proto.messages.AbstractMessage.QOSType;
import io.moquette.proto.messages.PublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.FilesystemConfig;

public class MQTTBroker extends HomeAutomationModule {
	private static final String MQTT_BROKER = "MQTTBroker";
	static Logger log = Logger.getLogger(MQTTBroker.class.getName());
	final Server mqttBroker = new Server();

	public MQTTBroker(ChannelController controller) {
		super(controller);

		// controller.getConfig().setLogFileForAppender("MQTTBroker",MQTTBroker.class.getName(),"moquette.log",Level.DEBUG);

		final List<? extends InterceptHandler> userHandlers = Arrays.asList(new PublisherListener(controller));
		try {
			File configFile = new File(controller.getConfig().getConfigFolder() + "/moquette.conf");
			FilesystemConfig classPathConfig = new FilesystemConfig(configFile);
			mqttBroker.startServer(classPathConfig, userHandlers);

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
		// {"data":{
		// HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE:{
		// "protocol":"MQTT",
		// "clientID":"posicat212701762",
		// "payload":"38000,1,69,341,171,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,21,64,21,64,21,64,21,64,21,64,21,64,21,64,21,21,21,21,21,64,21,21,21,64,21,21,21,21,21,21,21,21,21,64,21,21,21,64,21,21,21,64,21,64,21,64,21,64,21,1558,341,86,21,3650",
		// "topic":"home/commands/IR_GC"
		// },
		// },"channel":"MQTTBroker","source":"*"}

		if (incoming.hasData()) {
			JSONObject nativeDevice = incoming.getDataJObj(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE);

			PublishMessage message = new PublishMessage();
			message.setTopicName(nativeDevice.getString("topic"));
			message.setRetainFlag(false);
			message.setQos(QOSType.EXACTLY_ONCE);
			message.setPayload(ByteBuffer.wrap(nativeDevice.getString("payload").getBytes()));

			mqttBroker.internalPublish(message);
		}
	}

	class PublisherListener extends AbstractInterceptHandler {
		private NodeInterfaceString hubInterface;

		public PublisherListener(ChannelController controller) {
			this.hubInterface = new NodeInterfaceString(controller, MQTT_BROKER);
		}

		@Override
		public void onPublish(InterceptPublishMessage message) {
			String payload = new String(message.getPayload().array());
			System.out.println("moquette mqtt broker message intercepted, topic: " + message.getTopicName() + ", content: " + payload);

			HomeAutomationPacket hap = new HomeAutomationPacket();
			hap.setSource(MQTT_BROKER);
			hap.setDestination(HomeAutomationPacket.CHANNEL_EVENT_HANDLER);
			JSONObject nativeDevice = new JSONObject();
			nativeDevice.put("protocol", "MQTT");
			nativeDevice.put("topic", message.getTopicName());
			nativeDevice.put("clientID", message.getClientID());
			nativeDevice.put("payload", payload);
			hap.putData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE, nativeDevice);
			hap.putData(HomeAutomationPacket.FIELD_RESOLUTION, HomeAutomationPacket.RESOLUTION_TO_COMMON);

			hubInterface.sendDataPacketToController(hap);
		}
	}
}
