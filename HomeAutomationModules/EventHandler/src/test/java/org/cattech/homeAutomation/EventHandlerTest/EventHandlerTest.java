package org.cattech.homeAutomation.EventHandlerTest;

import java.util.List;
import java.util.Stack;

import org.cattech.homeAutomation.EventHandler.EventHandler;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;

public class EventHandlerTest extends BaseTestForModules {
	EventHandler eventHandler = Mockito.mock(EventHandler.class);

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		eventHandler = new EventHandler(controller);
		new Thread(eventHandler, "Testing EventHandler").start();
		registerChannel(testInterface, new String[] { "DeviceResolver"});
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		eventHandler.setRunning(false);
	}

	@Test
	public void testForwardsNativeDevicesToResolver() {

		HomeAutomationPacket hap = new HomeAutomationPacket("{\"destination\":[\"EventHandler\"]," + testPacketSource + ",\"data\":{\"nativeDevice\":\"\"}}");
		testInterface.sendDataPacketToController(hap);

		String result = waitforResultPacket(testInterface, (long) MAX_TEST_WAIT).toString();

		JSONAssert.assertEquals(
				"{\"nodeName\":\"EventHandler\",\"data\":{\"resolution\":\"toCommon\",\"nativeDevice\":\"\"},\"channel\":\"DeviceResolver\",\"source\":\"EventHandler\"}s",
				result, false);
	}

	//@Test
	public void testForwardsCommonDevicesToEventHandler() {

		HomeAutomationPacket hap = new HomeAutomationPacket("{\"destination\":[\"EventHandler\"]," + testPacketSource + ",\"data\":{\"device\":[\"lamp\"]}}");
		testInterface.sendDataPacketToController(hap);

		String result = waitforResultPacket(testInterface, (long) MAX_TEST_WAIT).toString();

		List<JSONObject> expected = new Stack<JSONObject>();
		expected.add(new JSONObject("{\"nodeName\":\"EventHandler\",\"data\":{\"device\":[\"lamp\"]},\"channel\":\"EventHandler\",\"source\":\"EventHandler\"}"));
		
		Mockito.when(eventHandler.getActionsForEvent(Mockito.any())).thenReturn(expected);
		
		JSONAssert.assertEquals(
				"{\"nodeName\":\"EventHandler\",\"data\":{\"device\":[\"lamp\"]},\"channel\":\"EventHandler\",\"source\":\"EventHandler\"}",
				result, false);
	}
}
