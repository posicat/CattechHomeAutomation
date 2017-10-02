package org.cattech.homeAutomation.communicationHubTest;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.configuration.homeAutomationConfiguration;
import org.junit.Before;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

public class BaseTestWithController {
	private Logger log = Logger.getLogger(this.getClass());
	
	protected static final String	TESTCHANNEL			= "testResult";
	protected ChannelController		controller;
	protected NodeInterfaceString	testInterface;
	protected String				testPacketSource	= "\"source\":\"" + TESTCHANNEL + "\"";

	@Before
	protected void setUp() throws Exception {
		controller = new ChannelController(new homeAutomationConfiguration());
		
		testInterface = new NodeInterfaceString(controller);
		registerChannel(testInterface, TESTCHANNEL);
	}

	protected String registerChannel(NodeInterfaceString inter, String channel) {
		inter.sendDataToController("{\"register\":[\"" + channel + "\"],\"nodeName\":\"BaseTestWithController\",\"data\":{\"testrunner\":\"true\"}}");
		return waitforResult(inter, 1000 * 10);
	}

	protected String waitforResult(NodeInterfaceString inter, long timeout) {
		timeout += System.currentTimeMillis();
		String result = null;
		while (null == result && timeout > System.currentTimeMillis()) {
			result = inter.getDataFromController();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		return result;
	}

	protected void addTranslationToResolver(String nativeDevice, String commonDevice) {
		String message = 
				"{\"destination\":[\"DeviceResolver\"],"+testPacketSource+",\"data\":{"+
						"\"resolution\":\"addLookup\","+
						"\"nativeDevice\":"+nativeDevice+","+
						"\"commonDevice\":"+commonDevice+
						"}}";
		
		testInterface.sendDataToController(message);
		
		String result = waitforResult(testInterface,1000*10);
		log.info(result);
	}

	protected int assertResultIsInArray(String[] expected, String result) {
		for (int i = 0; i < expected.length; i++) {
			JSONCompareResult res = JSONCompare.compareJSON(expected[i],result,JSONCompareMode.LENIENT);
			if (res.passed()) {
				return i;
			}
		}
		return -1;
	}

}
