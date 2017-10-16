package EventHandlerTest;

import org.cattech.homeAutomation.EventHandler.EventHandler;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class EventHandlerTest extends BaseTestForModules {
	EventHandler eventHandler;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		eventHandler=new EventHandler(controller);
		new Thread(eventHandler, "Testing EventHandler").start();
		registerChannel(testInterface, new String[] { "DeviceResolver", "WebEventHandler" });
	}

	@Override
	@After
	public void tearDown() {
		super.tearDown();
		eventHandler.setRunning(false);
	}

	@Test
	public void testForwardsNativeDevicesToResolver() {

		testInterface.sendDataToController("{\"destination\":[\"EventHandler\"]," + testPacketSource + ","
				+ "\"data\":{\"nativeDevice\":\"somethingNative\"}}");

		String result = waitforResult(testInterface, 10000);

		JSONAssert.assertEquals(
				"{\"nodeName\":\"EventHandler\",\"data\":{\"nativeDevice\":\"somethingNative\"},\"channel\":\"DeviceResolver\",\"source\":\"EventHandler\"}s",
				result, false);
	}

	@Test
	public void testForwardsCommonDevicesToWebEventHandler() {

		testInterface.sendDataToController("{\"destination\":[\"EventHandler\"]," + testPacketSource + ","
				+ "\"data\":{\"device\":[\"lamp\"]}}");

		String result = waitforResult(testInterface, 10000);

		JSONAssert.assertEquals(
				"{\"nodeName\":\"EventHandler\",\"data\":{\"device\":[\"lamp\"]},\"channel\":\"WebEventHandler\",\"source\":\"EventHandler\"}",
				result, false);
	}
}
