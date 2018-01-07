package org.cattech.homeAutomation.CommandHandlerTest;

import org.cattech.homeAutomation.CommandHandler.CommandHandler;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
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
		registerChannel(testInterface, new String[] { "DeviceResolver", "EventHandler" });
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		commandHandler.setRunning(false);
	}

	@Test
	public void testForwardsNativeDevicesToDeviceResolver() {

		testInterface.sendDataToController("{\"destination\":[\"DeviceCommandHandler\"]," + testPacketSource + ","
				+ "\"data\":{\"nativeDevice\":\"\"}}");

		String result = waitforResult(testInterface, 10000);

		JSONAssert.assertEquals(
				"{\"nodeName\":\"DeviceCommandHandler\",\"data\":{\"nativeDevice\":\"\",\"resolution\":\"toNative\"},\"channel\":\"DeviceResolver\",\"source\":\"DeviceCommandHandler\"}",
				result, false);
	}

	@Test
	public void testForwardsCommonDevicesToResolverAsWell() {

		testInterface.sendDataToController("{\"destination\":[\"DeviceCommandHandler\"]," + testPacketSource + ","
				+ "\"data\":{\"device\":[\"lamp\"]}}");

		String result = waitforResult(testInterface, 10000);

		JSONAssert.assertEquals(
				"{\"nodeName\":\"DeviceCommandHandler\",\"data\":{\"device\":[\"lamp\"],\"resolution\":\"toNative\"},\"channel\":\"DeviceResolver\",\"source\":\"DeviceCommandHandler\"}",
				result, false);
	}
}
