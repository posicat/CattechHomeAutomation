package communicationHubTest;

import java.util.Properties;

import org.junit.Before;

import communicationHub.ChannelController;
import communicationHub.NodeInterfaceString;

public class BaseTestWithController {

	protected static final String TESTCHANNEL = "testResult";
	protected ChannelController controller;
	protected NodeInterfaceString testInterface;
	protected String testPacketSource = "\"source\":\""+TESTCHANNEL+"\"";

	@Before
	protected void setUp() throws Exception {
		controller = new ChannelController(new Properties());
		testInterface = new NodeInterfaceString(controller);
		registerChannel(testInterface,TESTCHANNEL);
	}

	protected String registerChannel(NodeInterfaceString inter,String channel) {
		inter.sendDataToController("{\"register\":[\""+channel+"\"],data:{\"testrunner\":\"true\"}}");
		return waitforResult(inter,(long) (1000*10));
	}
	
	protected String waitforResult(NodeInterfaceString inter,long timeout) {
		timeout += System.currentTimeMillis();
		String result=null;
		while (null==result && timeout > System.currentTimeMillis()) {
			result = inter.getDataFromController();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		return result;
	}

}
