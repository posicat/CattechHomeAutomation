package org.cattech.homeAutomation.DeviceEventHandlerTest;

import org.cattech.homeAutomation.DeviceEventHandler.DeviceEventHandler;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class DeviceEventHandlerTest extends BaseTestForModules {
	DeviceEventHandler deviceEventHandler;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		deviceEventHandler=new DeviceEventHandler(controller);
		new Thread(deviceEventHandler, "Testing DeviceEventHandler").start();
		registerChannel(testInterface, new String[] { "DeviceResolver", "WebEventHandler" });
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		deviceEventHandler.setRunning(false);
	}

	@Test
	public void testForwardsNativeDevicesToResolver() {

		testInterface.sendDataToController("{\"destination\":[\"DeviceEventHandler\"]," + testPacketSource + ",\"data\":{\"nativeDevice\":\"\"}}");

		String result = waitforResult(testInterface, 10000);

		JSONAssert.assertEquals(
				"{\"nodeName\":\"DeviceEventHandler\",\"data\":{\"postResolv\":\"WebEventHandler\",\"resolution\":\"toCommon\",\"nativeDevice\":\"\"},\"channel\":\"DeviceResolver\",\"source\":\"DeviceEventHandler\"}s",
				result, false);
	}

	@Test
	public void testForwardsCommonDevicesToWebEventHandler() {

		testInterface.sendDataToController("{\"destination\":[\"DeviceEventHandler\"]," + testPacketSource + ",\"data\":{\"device\":[\"lamp\"]}}");

		String result = waitforResult(testInterface, 10000);

		JSONAssert.assertEquals(
				"{\"nodeName\":\"DeviceEventHandler\",\"data\":{\"device\":[\"lamp\"]},\"channel\":\"WebEventHandler\",\"source\":\"DeviceEventHandler\"}",
				result, false);
	}
}
