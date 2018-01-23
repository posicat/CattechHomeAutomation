package org.cattech.homeAutomation.communicationHubTest;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import junit.framework.Assert;

public class BaseTestWithController {
	private Logger log = Logger.getLogger(this.getClass());
	public static final int MAX_TEST_WAIT = 1500;

	protected static final String TESTCHANNEL = "testResult";
	protected ChannelController controller;
	protected NodeInterfaceString testInterface;

	// @Deprecated
	protected String testPacketSource = "\"source\":\"" + TESTCHANNEL + "\"";

	@Before
	protected void setUp() throws Exception {
		controller = new ChannelController(new HomeAutomationConfiguration(false));

		testInterface = new NodeInterfaceString(controller);
		registerChannel(testInterface, new String[] { TESTCHANNEL });
	}

	@After
	public void tearDown() throws Exception {

	}

	protected String registerChannel(NodeInterfaceString inter, String[] channels) {
		JSONArray channelArr = new JSONArray(channels);
		inter.sendDataToController("{\"register\":" + channelArr + ",\"nodeName\":\"BaseTestWithController\",\"data\":{\"testrunner\":\"true\"}}");
		return waitforResult(inter, MAX_TEST_WAIT);
	}

	protected String waitforResult(NodeInterfaceString inter, long timeout) {
		timeout += System.currentTimeMillis();
		String result = null;
		while (null == result && timeout > System.currentTimeMillis()) {
			result = inter.getDataFromController();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		return result;
	}

	protected void addTranslationToResolver(String nativeDevice, String commonDevice) {
		String message = "{\"destination\":[\"DeviceResolver\"]," + testPacketSource + ",\"data\":{" + "\"resolution\":\"addLookup\"," + "\""+HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE+"\":" + nativeDevice + "," + "\""+HomeAutomationPacket.FIELD_DATA_DEVICE+"\":" + commonDevice
				+ "}}";

		testInterface.sendDataToController(message);

		String result = waitforResult(testInterface, MAX_TEST_WAIT);
		log.info(result);
	}

	protected void assertResultIsInArray(String[] expected, String result) {
		for (int i = 0; i < expected.length; i++) {
			JSONCompareResult res = JSONCompare.compareJSON(expected[i], result, JSONCompareMode.LENIENT);
			if (res.passed()) {
				return;
			}
		}

		String msg = " Did not find :\n";
		msg += "\t" + result.toString() + "\n";
		msg += "In any of the following (expected):\n";

		for (int i = 0; i < expected.length; i++) {
			msg += "\t" + expected[i] + "\n";
		}

		fail(msg);
	}

}
