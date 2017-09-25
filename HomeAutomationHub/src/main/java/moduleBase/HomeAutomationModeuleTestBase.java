package moduleBase;

import java.util.Properties;

import org.junit.Before;

import communicationHub.ChannelController;
import communicationHub.NodeInterfaceString;

public class HomeAutomationModeuleTestBase {

	protected ChannelController controller;
	protected NodeInterfaceString testInterface;
	protected String testPacketSource="\"source\":\"TestResult\"";

	@Before
	protected void setUp() throws Exception {
		controller = new ChannelController(new Properties());
		testInterface = new NodeInterfaceString(controller);
		
		testInterface.sendDataToController("{\"register\":[\"testResult\"]}");
		String result = waitforResult(1000*10);
		System.out.println(result);
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
