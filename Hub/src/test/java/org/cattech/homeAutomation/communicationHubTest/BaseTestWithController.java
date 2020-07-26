package org.cattech.homeAutomation.communicationHubTest;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.common.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;


public class BaseTestWithController {
	private Logger log = Logger.getLogger(this.getClass());
	public static final int MAX_TEST_WAIT = 5000;

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
		HomeAutomationPacket hap = new HomeAutomationPacket("{\"register\":" + channelArr + ",\"nodeName\":\"BaseTestWithController\",\"data\":{\"testrunner\":\"true\"}}");
		inter.sendDataPacketToController(hap);
		return waitforResultPacket(inter, MAX_TEST_WAIT).toString();
	}

	protected HomeAutomationPacket waitforResultPacket(NodeInterfaceString inter, long timeout) {
		timeout += System.currentTimeMillis();
		HomeAutomationPacket result = null;
		while (null == result && timeout > System.currentTimeMillis()) {
			result = inter.getDataPacketFromController();
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

		HomeAutomationPacket hap = new HomeAutomationPacket(message);
		testInterface.sendDataPacketToController(hap);

		hap = waitforResultPacket(testInterface, MAX_TEST_WAIT);
		log.info(hap);
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
