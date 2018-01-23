package org.cattech.homeAutomation.mqttBrokerTest;

import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.cattech.homeAutomation.mqttBroker.MQTTBroker;
import org.junit.After;
import org.junit.Before;

public class MQTTBrokerTest extends BaseTestForModules {
	MQTTBroker broker;
	String commonDevMatches1 = "[\"upstairs\",\"marks bedroom\",\"nightstand\",\"lamp\"]";
	String nativeDevMatches1 = "{\"source\":\"x10\",\"house\":\"B\",\"unit\":\"9\",\"controlChannel\":\"channelNative1\"}";

	String commonDevMatches2 = "[\"upstairs\",\"mals bedroom\",\"lamp\"]";
	String nativeDevMatchA = "{\"source\":\"x10\",\"house\":\"B\",\"unit\":\"10\",\"controlChannel\":\"channelNativeA\"}";
	String nativeDevMatchB = "{\"source\":\"x10\",\"house\":\"B\",\"unit\":\"11\",\"controlChannel\":\"channelNativeB\"}";

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		broker = new MQTTBroker(controller);
		new Thread(broker, "Testing DeviceResolver").start();

		addTranslationToResolver(nativeDevMatches1, commonDevMatches1);
		addTranslationToResolver(nativeDevMatchA, commonDevMatches2);
		addTranslationToResolver(nativeDevMatchB, commonDevMatches2);

		registerChannel(testInterface, new String[] { "testEventHandler" });
		registerChannel(testInterface, new String[] { "channelNative1" });
		registerChannel(testInterface, new String[] { "channelNativeA" });
		registerChannel(testInterface, new String[] { "channelNativeB" });
	}

	@Override
	@After
	public void tearDown() {
		broker.setRunning(false);
	}
}
