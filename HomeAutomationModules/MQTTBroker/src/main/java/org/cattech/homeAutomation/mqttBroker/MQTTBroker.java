package org.cattech.homeAutomation.mqttBroker;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.ClasspathConfig;
import io.moquette.server.config.IConfig;

public class MQTTBroker extends HomeAutomationModule {
	private static final String MQTT_BROKER = "MQTTBroker";
	static Logger log = Logger.getLogger(MQTTBroker.class.getName());
	final IConfig classPathConfig = new ClasspathConfig();
	final Server mqttBroker = new Server();
	
	public MQTTBroker(ChannelController controller) {
		super(controller);
		final List<? extends InterceptHandler> userHandlers = Arrays.asList(new PublisherListener(controller));
		try {
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
			log.error("Error starting MQTT Broker",e);
		}
	}

	@Override
	public String getModuleChannelName() {
		return MQTT_BROKER;
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (incoming.getData().has("resolution")) {
		}
	}
	
	class PublisherListener extends AbstractInterceptHandler {
		private ChannelController controller;
		private NodeInterfaceString hubInterface;

		public PublisherListener(ChannelController controller) {
			this.controller=controller;
			this.hubInterface = new NodeInterfaceString(controller,MQTT_BROKER);
		}

		@Override
		public void onPublish(InterceptPublishMessage message) {
			System.out.println("moquette mqtt broker message intercepted, topic: " + message.getTopicName()
					+ ", content: " + new String(message.getPayload().array()));
			HomeAutomationPacket hap = new HomeAutomationPacket(MQTT_BROKER,null);
			hubInterface.sendDataPacketToController(hap, MQTT_BROKER);
		}
	}
}
