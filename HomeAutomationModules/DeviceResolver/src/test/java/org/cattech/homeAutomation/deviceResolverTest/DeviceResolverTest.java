package org.cattech.homeAutomation.deviceResolverTest;

import static org.junit.Assert.*;

import org.cattech.homeAutomation.deviceResolver.DeviceResolver;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

public class DeviceResolverTest extends BaseTestForModules {
	DeviceResolver deviceResolver;
	String commonDevMatches1 = "[\"upstairs\",\"marks bedroom\",\"nightstand\",\"lamp\"]";
	String nativeDevMatches1 = "{\"source\":\"x10\",\"house\":\"B\",\"unit\":\"9\"}";

	String commonDevMatches2 = "[\"upstairs\",\"mals bedroom\",\"lamp\"]";
	String nativeDevMatchA = "{\"source\":\"x10\",\"house\":\"B\",\"unit\":\"10\"}";
	String nativeDevMatchB = "{\"source\":\"x10\",\"house\":\"B\",\"unit\":\"11\"}";

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		deviceResolver = new DeviceResolver(controller);
		new Thread(deviceResolver, "Testing DeviceResolver").start();

		addTranslationToResolver(nativeDevMatches1, commonDevMatches1);
		addTranslationToResolver(nativeDevMatchA, commonDevMatches2);
		addTranslationToResolver(nativeDevMatchB, commonDevMatches2);

		registerChannel(testInterface, "testEventHandler");
	}

	@After
	public void tearDown() throws Exception {
		deviceResolver.setRunning(false);
	}

	@Test
	public void testCanDecodeNativeDeviceToCommon() {

		testInterface.sendDataToController("{\"destination\":[\"DeviceResolver\"]," + testPacketSource + ","
				+ "\"data\":{\"resolution\":\"toCommon\",\"postResolv\":\"testEventHandler\",\"nativeDevice\":"
				+ nativeDevMatches1 + ",\"action\":\"on\"}}");

		String result = waitforResult(testInterface, 10000);

		JSONAssert.assertEquals("{\"nodeName\":\"DeviceResolver\",\"data\":{\"action\":\"on\"," + "\"device\":"
				+ commonDevMatches1 + "},\"channel\":\"testEventHandler\",\"source\":\"DeviceResolver\"}", result,
				false);
		System.out.println(result);
	}

	@Test
	public void testCanDecodeCommonDeviceToNative() {

		testInterface.sendDataToController("{\"destination\":[\"DeviceResolver\"]," + testPacketSource + ","
				+ "\"data\":{\"resolution\":\"toNative\",\"postResolv\":\"testEventHandler\",\"device\":"
				+ commonDevMatches1 + ",\"action\":\"on\"}}");

		String result = waitforResult(testInterface, 10000);

		JSONAssert.assertEquals(
				"{\"nodeName\":\"DeviceResolver\",\"data\":{\"action\":\"on\"," + "\"nativeDevice\":"
						+ nativeDevMatches1 + "},\"channel\":\"testEventHandler\",\"source\":\"DeviceResolver\"}",
				result, false);
		System.out.println(result);
	}

	@Test
	public void testCanDecodeCommonDeviceToMultipleNative() {

		testInterface.sendDataToController("{\"destination\":[\"DeviceResolver\"]," + testPacketSource + ","
				+ "\"data\":{\"resolution\":\"toNative\",\"postResolv\":\"testEventHandler\",\"device\":"+commonDevMatches2+",\"action\":\"on\"}}");


		String[] expected = {
				"{\"nodeName\":\"DeviceResolver\",\"data\":{\"action\":\"on\","
				        + "\"nativeDevice\":"+nativeDevMatchA+"},\"channel\":\"testEventHandler\",\"source\":\"DeviceResolver\"}",
				"{\"nodeName\":\"DeviceResolver\",\"data\":{\"action\":\"on\","
						+ "\"nativeDevice\":"+nativeDevMatchB+"},\"channel\":\"testEventHandler\",\"source\":\"DeviceResolver\"}"
		};

		String result1 = waitforResult(testInterface, 10000);
		String result2 = waitforResult(testInterface, 10000);

		int m1 = assertMatchesOne(expected,result1);
		int m2 = assertMatchesOne(expected,result2);
		
		assertTrue(m1!=-1);
		assertTrue(m2!=-1);
		
		assertTrue(m1!=m2);
		assertTrue(m1+m2==3);
	}

	private int assertMatchesOne(String[] expected, String result) {
		for (int i = 0; i < expected.length; i++) {
			JSONCompareResult res = JSONCompare.compareJSON(expected[1],result,JSONCompareMode.LENIENT);
			if (res.passed()) {
				return i;
			}
		}
		return -1;
	}

}
