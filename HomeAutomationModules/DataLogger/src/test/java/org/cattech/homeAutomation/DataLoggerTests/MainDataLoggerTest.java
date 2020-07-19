package org.cattech.homeAutomation.DataLoggerTests;

import org.cattech.homeAutomation.DataLogger.DataLogger;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.junit.After;
import org.junit.Before;

public class MainDataLoggerTest extends BaseTestForModules {
	DataLogger dataLogger;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		dataLogger = new DataLogger(controller);
		new Thread(dataLogger, "Testing DeviceCommandHandler").start();
		registerChannel(testInterface, new String[] { HomeAutomationPacket.CHANNEL_DEVICE_RESOLVER, HomeAutomationPacket.CHANNEL_EVENT_HANDLER });
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		dataLogger.setRunning(false);
	}
	
//	@Test
//	public void testForwardsNativeDevicesToDeviceResolver() {
//
//		HomeAutomationPacket send = new HomeAutomationPacket();
//		send.addDestination("DeviceCommandHandler");
//		send.putWrapper(HomeAutomationPacket.FIELD_SOURCE, TESTCHANNEL);
//		send.putData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE, "device");
//		testInterface.sendDataPacketToController(send);
//
//		String result = waitforResultPacket(testInterface, (long) MAX_TEST_WAIT).toString();
//
//		JSONAssert.assertEquals(
//				"{\"nodeName\":\"DeviceCommandHandler\",\"data\":{\"nativeDevice\":\"device\",\"resolution\":\"toNative\"},\"channel\":\"DeviceResolver\",\"source\":\"DeviceCommandHandler\"}",
//				result, false);
//	}
//
//	@Test
//	public void testForwardsCommonDevicesToResolverAsWell() {
//
//		HomeAutomationPacket send = new HomeAutomationPacket();
//		send.addDestination("DeviceCommandHandler");
//		send.putWrapper(HomeAutomationPacket.FIELD_SOURCE, TESTCHANNEL);
//		send.putData(HomeAutomationPacket.FIELD_DATA_DEVICE, new JSONArray("[lamp]"));
//		testInterface.sendDataPacketToController(send);
//
//		String result = waitforResultPacket(testInterface, (long) MAX_TEST_WAIT).toString();
//
//		JSONAssert.assertEquals(
//				"{\"nodeName\":\"DeviceCommandHandler\",\"data\":{\"device\":[\"lamp\"],\"resolution\":\"toNative\"},\"channel\":\"DeviceResolver\",\"source\":\"DeviceCommandHandler\"}",
//				result, false);
//	}
}
