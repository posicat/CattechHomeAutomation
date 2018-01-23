package org.cattech.homeAutomation.CommandHandlerTest;

import org.cattech.homeAutomation.CommandHandler.CommandHandler;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class CommandHandlerTest extends BaseTestForModules {
	CommandHandler commandHandler;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		commandHandler = new CommandHandler(controller);
		new Thread(commandHandler, "Testing DeviceCommandHandler").start();
		registerChannel(testInterface, new String[] { HomeAutomationPacket.CHANNEL_DEVICE_RESOLVER, HomeAutomationPacket.CHANNEL_EVENT_HANDLER });
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		commandHandler.setRunning(false);
	}

	@Test
	public void testForwardsNativeDevicesToDeviceResolver() {

		HomeAutomationPacket send = new HomeAutomationPacket();
		send.addDestination("DeviceCommandHandler");
		send.putWrapper(HomeAutomationPacket.FIELD_SOURCE, TESTCHANNEL);
		send.putData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE, "device");
		testInterface.sendDataPacketToController(send);

		String result = waitforResult(testInterface, MAX_TEST_WAIT);

		JSONAssert.assertEquals(
				"{\"nodeName\":\"DeviceCommandHandler\",\"data\":{\"nativeDevice\":\"device\",\"resolution\":\"toNative\"},\"channel\":\"DeviceResolver\",\"source\":\"DeviceCommandHandler\"}",
				result, false);
	}

	@Test
	public void testForwardsCommonDevicesToResolverAsWell() {

		HomeAutomationPacket send = new HomeAutomationPacket();
		send.addDestination("DeviceCommandHandler");
		send.putWrapper(HomeAutomationPacket.FIELD_SOURCE, TESTCHANNEL);
		send.putData(HomeAutomationPacket.FIELD_DATA_DEVICE, new JSONArray("[lamp]"));
		testInterface.sendDataPacketToController(send);

		String result = waitforResult(testInterface, MAX_TEST_WAIT);

		JSONAssert.assertEquals(
				"{\"nodeName\":\"DeviceCommandHandler\",\"data\":{\"device\":[\"lamp\"],\"resolution\":\"toNative\"},\"channel\":\"DeviceResolver\",\"source\":\"DeviceCommandHandler\"}",
				result, false);
	}
}
