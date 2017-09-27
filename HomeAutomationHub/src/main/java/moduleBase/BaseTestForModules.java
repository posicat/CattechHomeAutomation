package moduleBase;

import java.util.Properties;

import org.junit.Before;

import communicationHub.ChannelController;
import communicationHub.NodeInterfaceString;

public class HomeAutomationModeuleTestBase {

	private static final String TESTCHANNEL = "testResult";
	protected ChannelController controller;
	protected NodeInterfaceString testInterface;
	protected String testPacketSource="\"source\":\""+TESTCHANNEL+"\"";

	@Before
	protected void setUp() throws Exception {
		controller = new ChannelController(new Properties());
		testInterface = new NodeInterfaceString(controller);
		registerChannel(TESTCHANNEL);
	}

	protected void registerChannel(String channel) {
		testInterface.sendDataToController("{\"register\":[\""+channel+"\"],data:{\"testrunner\":\"true\"}}");
		String result = waitforResult(1000*10);
	}
	
	protected void addTranslationToResolver(String nativeDevice, String commonDevice) {
		String message = 
				"{\"destination\":[\"DeviceResolver\"],"+testPacketSource+",\"data\":{"+
						"\"resolution\":\"addLookup\","+
						"\"nativeDevice\":"+nativeDevice+","+
						"\"commonDevice\":"+commonDevice+
						"}}";
		
		testInterface.sendDataToController(message);
		
		String result = waitforResult(1000*10);
		System.out.println(result);
	}

	protected String waitforResult(long timeout) {
		timeout += System.currentTimeMillis();
		String result=null;
		while (null==result && timeout > System.currentTimeMillis()) {
			result = testInterface.getDataFromController();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		return result;
	}

}
