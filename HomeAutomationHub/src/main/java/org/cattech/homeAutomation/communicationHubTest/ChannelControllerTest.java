package org.cattech.homeAutomation.communicationHubTest;

import static org.junit.Assert.assertEquals;

import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class ChannelControllerTest extends BaseTestWithController {
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testRegisterChannel() throws Exception {
		NodeInterfaceString testInterface = new NodeInterfaceString(controller);

		String result = registerChannel(testInterface,"a");

		JSONAssert.assertEquals("{\"source\":\"ChannelController\",\"status\":\"registered\",\"channel\":[\"a\"],\"nodeName\":\""
				+ testInterface.getNodeName() + "\"}", result, true);
	}

	@Test
	public void testSendDataToChannel() throws Exception {
		NodeInterfaceString testInterface = new NodeInterfaceString(controller);

		String result = registerChannel(testInterface,"a");

		JSONAssert.assertEquals("{\"source\":\"ChannelController\",\"status\":\"registered\",\"channel\":[\"a\"],\"nodeName\":\""
				+ testInterface.getNodeName() + "\"}", result, true);

		testInterface.sendDataToController("{\"destination\":[\"a\"],\"source\":\"a\",\"data\":{\"test\":\"success\"}}");

		result = testInterface.getDataFromController();
		JSONAssert.assertEquals("{\"source\":\"a\",\"channel\":\"a\",\"nodeName\":\"" + testInterface.getNodeName()
				+ "\",\"data\":{\"test\":\"success\"}}", result, true);
	}

	@Test
	public void testSendDataToDifferentChannel() throws Exception {
		NodeInterfaceString testInterface = new NodeInterfaceString(controller);

		String result = registerChannel(testInterface,"a");

		JSONAssert.assertEquals("{\"source\":\"ChannelController\",\"status\":\"registered\",\"channel\":[\"a\"],\"nodeName\":\""
				+ testInterface.getNodeName() + "\"}", result, true);
		
		testInterface.sendDataToController("{\"destination\":[\"b\"],\"source\":\"a\",\"data\":{\"test\":\"should not send, no node b\"}}");
		//This will display a "No node regitered" error on the server, can be ignored.

		result = testInterface.getDataFromController();
		assertEquals(null, result);
	}

	@Test
	public void testSendDataToMultipleChannels() throws Exception {
		NodeInterfaceString testInterfaceA = new NodeInterfaceString(controller);
		NodeInterfaceString testInterfaceB = new NodeInterfaceString(controller);

		String result = registerChannel(testInterfaceA,"a");

		JSONAssert.assertEquals("{\"source\":\"ChannelController\",\"status\":\"registered\",\"channel\":[\"a\"],\"nodeName\":\""
				+ testInterfaceA.getNodeName() + "\"}", result, true);

		result = registerChannel(testInterfaceB,"b");
		JSONAssert.assertEquals("{\"source\":\"ChannelController\",\"status\":\"registered\",\"channel\":[\"b\"],\"nodeName\":\""
				+ testInterfaceB.getNodeName() + "\"}", result, true);

		// Send from A to A, should be received by A only
		testInterfaceA.sendDataToController("{\"destination\":[\"a\"],\"source\":\"a\",\"data\":{\"test\":\"success1\"}}");

		result = testInterfaceA.getDataFromController();
		JSONAssert.assertEquals("{\"source\":\"a\",\"channel\":\"a\",\"nodeName\":\"" + testInterfaceA.getNodeName()
				+ "\",\"data\":{\"test\":\"success1\"}}", result, true);

		result = testInterfaceB.getDataFromController();
		assertEquals(null, result);

		// Send from A to B, should be received by B only
		testInterfaceA.sendDataToController("{\"destination\":[\"b\"],\"source\":\"a\",\"data\":{\"test\":\"success2\"}}");

		result = testInterfaceA.getDataFromController();
		assertEquals(null, result);

		result = testInterfaceB.getDataFromController();
		JSONAssert.assertEquals("{\"source\":\"a\",\"channel\":\"b\",\"nodeName\":\"" + testInterfaceA.getNodeName()
				+ "\",\"data\":{\"test\":\"success2\"}}", result, true);

		// Send from B to A, should be received by A only
		testInterfaceB.sendDataToController("{\"destination\":[\"a\"],\"source\":\"b\",\"data\":{\"test\":\"success3\"}}");

		result = testInterfaceA.getDataFromController();
		JSONAssert.assertEquals("{\"source\":\"b\",\"channel\":\"a\",\"nodeName\":\"" + testInterfaceB.getNodeName()
				+ "\",\"data\":{\"test\":\"success3\"}}", result, true);

		result = testInterfaceB.getDataFromController();
		assertEquals(null, result);
	}
}
