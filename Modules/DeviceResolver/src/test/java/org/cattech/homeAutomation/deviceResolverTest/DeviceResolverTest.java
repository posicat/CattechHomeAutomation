package org.cattech.homeAutomation.deviceResolverTest;

import static org.junit.Assert.assertNotNull;

import org.cattech.homeAutomation.deviceResolver.DeviceResolver;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class DeviceResolverTest extends BaseTestForModules {
	
	DeviceResolver deviceResolver;
	String commonDevMatches1 = "[\"upstairs\",\"marks bedroom\",\"nightstand\",\"lamp\"]";
	String nativeDevMatches1 = "{\"source\":\"x10\",\"house\":\"B\",\"unit\":\"9\",\"controlChannel\":\"channelNative1\"}";

	String commonDevMatches2 = "[\"upstairs\",\"mals bedroom\",\"lamp\"]";
	String nativeDevMatchA = "{\"source\":\"x10\",\"house\":\"B\",\"unit\":\"10\",\"controlChannel\":\"channelNativeA\"}";
	String nativeDevMatchB = "{\"source\":\"x10\",\"house\":\"B\",\"unit\":\"11\",\"controlChannel\":\"channelNativeB\"}";

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		deviceResolver = new DeviceResolver(controller);
		new Thread(deviceResolver, "Testing DeviceResolver").start();

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
		deviceResolver.setRunning(false);
	}

	@Test
	public void testCanDecodeNativeDeviceToCommon() {

		HomeAutomationPacket hap = new HomeAutomationPacket("{\"destination\":[\"DeviceResolver\"]," + testPacketSource + ","
						+ "\"data\":{\"resolution\":\"toCommon\",\"postResolve\":\"testEventHandler\",\"nativeDevice\":"
						+ nativeDevMatches1 + ",\"action\":\"on\"}}");
		testInterface.sendDataPacketToController(hap);

		String result = waitforResultPacket(testInterface, (long) MAX_TEST_WAIT).toString();

		String expectedStr = "{\"nodeName\":\"DeviceResolver\",\"data\":{\"action\":\"on\"," + "\"device\":"
				+ commonDevMatches1 + "},\"channel\":\"testEventHandler\",\"source\":\"DeviceResolver\"}";
		System.out.println(expectedStr);
		System.out.println(result);
		JSONAssert.assertEquals(expectedStr, result, false);
	}

	@Test
	public void testCanDecodeCommonDeviceToNative() {

		HomeAutomationPacket hap = new HomeAutomationPacket("{\"destination\":[\"DeviceResolver\"]," + testPacketSource + ","
						+ "\"data\":{\"resolution\":\"toNative\",\"postResolve\":\"testEventHandler\",\"device\":"
						+ commonDevMatches1 + ",\"action\":\"on\"}}");
		testInterface.sendDataPacketToController(hap);

		String result = waitforResultPacket(testInterface, (long) MAX_TEST_WAIT).toString();

		JSONAssert.assertEquals(
				"{\"nodeName\":\"DeviceResolver\",\"data\":{\"action\":\"on\"," + "\"nativeDevice\":"
						+ nativeDevMatches1 + "},\"channel\":\"testEventHandler\",\"source\":\"DeviceResolver\"}",
				result, false);
	}

	@Test
	public void testCanDecodeCommonDeviceToNativeWithNoPostResolveGoesToNative() {

		HomeAutomationPacket hap = new HomeAutomationPacket("{\"destination\":[\"DeviceResolver\"]," + testPacketSource + ","
						+ "\"data\":{\"resolution\":\"toNative\",\"device\":" + commonDevMatches1 + ",\"action\":\"on\"}}");
		testInterface.sendDataPacketToController(hap);

		HomeAutomationPacket result = waitforResultPacket(testInterface, (long) MAX_TEST_WAIT);
		
	    assertNotNull(result);
		JSONAssert.assertEquals(
				"{\"nodeName\":\"DeviceResolver\",\"data\":{\"action\":\"on\"," + "\"nativeDevice\":"
						+ nativeDevMatches1 + "},\"channel\":\"channelNative1\",\"source\":\"DeviceResolver\"}",
				result.toString(), false);
	}

	@Test
	public void testCanDecodeCommonDeviceToMultipleNative() {

		HomeAutomationPacket hap = new HomeAutomationPacket("{\"destination\":[\"DeviceResolver\"]," + testPacketSource + ","
						+ "\"data\":{\"resolution\":\"toNative\",\"postResolve\":\"testEventHandler\",\"device\":"
						+ commonDevMatches2 + ",\"action\":\"on\"}}");
		testInterface.sendDataPacketToController(hap);

		String[] expected = {
				"{\"nodeName\":\"DeviceResolver\",\"data\":{\"action\":\"on\"," + "\"nativeDevice\":" + nativeDevMatchA
						+ "},\"channel\":\"testEventHandler\",\"source\":\"DeviceResolver\"}",
				"{\"nodeName\":\"DeviceResolver\",\"data\":{\"action\":\"on\"," + "\"nativeDevice\":" + nativeDevMatchB
						+ "},\"channel\":\"testEventHandler\",\"source\":\"DeviceResolver\"}" };

		String result1 = waitforResultPacket(testInterface, (long) MAX_TEST_WAIT).toString();
		String result2 = waitforResultPacket(testInterface, (long) MAX_TEST_WAIT).toString();

		assertResultIsInArray(expected, result1);
		assertResultIsInArray(expected, result2);
	}

	@Test
	public void testCanDecodeCommonDeviceToMultipleNativeWithNoPostResolveGoesToNative() {

		HomeAutomationPacket hap = new HomeAutomationPacket("{\"destination\":[\"DeviceResolver\"]," + testPacketSource + ","
						+ "\"data\":{\"resolution\":\"toNative\",\"device\":" + commonDevMatches2 + ",\"action\":\"on\"}}");
		testInterface.sendDataPacketToController(hap);

		String[] expected = {
				"{\"nodeName\":\"DeviceResolver\",\"data\":{\"action\":\"on\"," + "\"nativeDevice\":" + nativeDevMatchA
						+ "},\"channel\":\"channelNativeA\",\"source\":\"DeviceResolver\"}",
				"{\"nodeName\":\"DeviceResolver\",\"data\":{\"action\":\"on\"," + "\"nativeDevice\":" + nativeDevMatchB
						+ "},\"channel\":\"channelNativeB\",\"source\":\"DeviceResolver\"}" };

		String result1 = waitforResultPacket(testInterface, (long) MAX_TEST_WAIT).toString();
		String result2 = waitforResultPacket(testInterface, (long) MAX_TEST_WAIT).toString();

		assertResultIsInArray(expected, result1);
		assertResultIsInArray(expected, result2);
	}

}



