package org.cattech.homeAutomation.MessagingTest;

import org.cattech.homeAutomation.Messaging.Messaging;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.junit.After;
import org.junit.Before;

public class MessagingTest extends BaseTestForModules {
	Messaging messaging;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		messaging = new Messaging(controller);
		new Thread(messaging, "Testing DeviceEventHandler").start();
		registerChannel(testInterface, new String[] { "DeviceResolver", "EventHandler" });
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		messaging.setRunning(false);
	}

//	@Test
//	public void testForwardsNativeDevicesToResolver() {
//
//		testInterface.sendDataToController(
//				"{\"destination\":[\"DeviceEventHandler\"]," + testPacketSource + ",\"data\":{\"nativeDevice\":\"\"}}");
//
//		String result = waitforResult(testInterface, 10000);
//
//		JSONAssert.assertEquals(
//				"{\"nodeName\":\"DeviceEventHandler\",\"data\":{\"resolution\":\"toCommon\",\"nativeDevice\":\"\"},\"channel\":\"DeviceResolver\",\"source\":\"DeviceEventHandler\"}s",
//				result, false);
//	}
//
//	@Test
//	public void testForwardsCommonDevicesToEventHandler() {
//
//		testInterface.sendDataToController(
//				"{\"destination\":[\"DeviceEventHandler\"]," + testPacketSource + ",\"data\":{\"device\":[\"lamp\"]}}");
//
//		String result = waitforResult(testInterface, 10000);
//
//		JSONAssert.assertEquals(
//				"{\"nodeName\":\"DeviceEventHandler\",\"data\":{\"device\":[\"lamp\"]},\"channel\":\"EventHandler\",\"source\":\"DeviceEventHandler\"}",
//				result, false);
//	}
}
