package org.cattech.homeAutomation.communicationHubTest;

import static org.junit.Assert.assertEquals;

import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
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

		String result = registerChannel(testInterface, new String[] { "a" });

		JSONAssert.assertEquals(
				"{\"source\":\"ChannelController\",\"status\":\"registered\",\"channel\":[\"a\"],\"nodeName\":\""
						+ testInterface.getNodeName() + "\"}",
				result, true);
	}

	@Test
	public void testSendDataToChannel() throws Exception {
		NodeInterfaceString testInterface = new NodeInterfaceString(controller);

		String result = registerChannel(testInterface, new String[] { "a" });

		JSONAssert.assertEquals(
				"{\"source\":\"ChannelController\",\"status\":\"registered\",\"channel\":[\"a\"],\"nodeName\":\""
						+ testInterface.getNodeName() + "\"}",
				result, true);

		HomeAutomationPacket hap = new HomeAutomationPacket("{\"destination\":[\"a\"],\"source\":\"a\",\"data\":{\"test\":\"success\"}}");
		testInterface.sendDataPacketToController(hap);

		HomeAutomationPacket hapResult = testInterface.getDataPacketFromController();
		JSONAssert.assertEquals("{\"source\":\"a\",\"channel\":\"a\",\"nodeName\":\"" + testInterface.getNodeName()
				+ "\",\"data\":{\"test\":\"success\"}}", hapResult.toString(), true);
	}

	@Test
	public void testSendDataToDifferentChannel() throws Exception {
		NodeInterfaceString testInterface = new NodeInterfaceString(controller);

		String result = registerChannel(testInterface, new String[] { "a" });

		JSONAssert.assertEquals(
				"{\"source\":\"ChannelController\",\"status\":\"registered\",\"channel\":[\"a\"],\"nodeName\":\""
						+ testInterface.getNodeName() + "\"}",
				result, true);

		HomeAutomationPacket hap = new HomeAutomationPacket("{\"destination\":[\"b\"],\"source\":\"a\",\"data\":{\"test\":\"should not send, no node b\"}}");
		testInterface.sendDataPacketToController(hap);
		// This will display a "No node regitered" error on the server, can be ignored.

		HomeAutomationPacket resultHAP = testInterface.getDataPacketFromController();
		assertEquals(null, resultHAP);
	}

	@Test
	public void testSendDataToMultipleChannels() throws Exception {
		NodeInterfaceString testInterfaceA = new NodeInterfaceString(controller);
		NodeInterfaceString testInterfaceB = new NodeInterfaceString(controller);

		String result = registerChannel(testInterfaceA, new String[] { "a" });

		JSONAssert.assertEquals(
				"{\"source\":\"ChannelController\",\"status\":\"registered\",\"channel\":[\"a\"],\"nodeName\":\""
						+ testInterfaceA.getNodeName() + "\"}",
				result, true);

		result = registerChannel(testInterfaceB, new String[] { "b" });
		JSONAssert.assertEquals(
				"{\"source\":\"ChannelController\",\"status\":\"registered\",\"channel\":[\"b\"],\"nodeName\":\""
						+ testInterfaceB.getNodeName() + "\"}",
				result, true);

		// Send from A to A, should be received by A only
		HomeAutomationPacket hap = new HomeAutomationPacket("{\"destination\":[\"a\"],\"source\":\"a\",\"data\":{\"test\":\"success1\"}}");
		testInterfaceA.sendDataPacketToController(hap);

		result = testInterfaceA.getDataPacketFromController().toString();
		JSONAssert.assertEquals("{\"source\":\"a\",\"channel\":\"a\",\"nodeName\":\"" + testInterfaceA.getNodeName()
				+ "\",\"data\":{\"test\":\"success1\"}}", result, true);

		HomeAutomationPacket resultHAP = testInterfaceB.getDataPacketFromController();
		assertEquals(null, resultHAP);

		// Send from A to B, should be received by B only
		hap = new HomeAutomationPacket("{\"destination\":[\"b\"],\"source\":\"a\",\"data\":{\"test\":\"success2\"}}");
		testInterfaceA.sendDataPacketToController(hap);

		resultHAP = testInterfaceA.getDataPacketFromController();
		assertEquals(null, resultHAP);

		result = testInterfaceB.getDataPacketFromController().toString();
		JSONAssert.assertEquals("{\"source\":\"a\",\"channel\":\"b\",\"nodeName\":\"" + testInterfaceA.getNodeName()
				+ "\",\"data\":{\"test\":\"success2\"}}", result, true);

		// Send from B to A, should be received by A only
		hap = new HomeAutomationPacket("{\"destination\":[\"a\"],\"source\":\"b\",\"data\":{\"test\":\"success3\"}}");
		testInterfaceB.sendDataPacketToController(hap);

		result = testInterfaceA.getDataPacketFromController().toString();
		JSONAssert.assertEquals("{\"source\":\"b\",\"channel\":\"a\",\"nodeName\":\"" + testInterfaceB.getNodeName()
				+ "\",\"data\":{\"test\":\"success3\"}}", result, true);

		resultHAP = testInterfaceB.getDataPacketFromController();
		assertEquals(null, resultHAP);
	}
}
