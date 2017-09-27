package deviceResolver;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import DeviceResolver.DeviceResolver;
import moduleBase.BaseTestForModules;

public class DeviceResolverTest extends BaseTestForModules {
	DeviceResolver deviceResolver;
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		testInterface.setFullTrace(true);
		
		deviceResolver = new DeviceResolver(controller);
		new Thread(deviceResolver, "Testing DeviceResolver").start();
				
		addTranslationToResolver(
				"{\"source\":\"heyu\",\"house\":\"B\",\"unit\":\"9\"}",
				"[\"upstairs\",\"posi's bedroom\",\"nightstand\",\"lamp\"]"
		);
		registerChannel(testInterface,"EventHandler");
	}


	@After
	public void tearDown() throws Exception {
		deviceResolver.setRunning(false);
	}

	@Test
	public void test() {

		testInterface.sendDataToController("{\"destination\":[\"DeviceResolver\"],"+testPacketSource+","+
		"\"data\":{\"resolution\":\"toCommon\",\"PostResolv\":\"EventHandler\",\"nativeDevice\":{\"source\":\"heyu\",\"house\":\"B\",\"unit\":\"9\"},\"action\":\"on\"}}");
		
		String result = waitforResult(testInterface,(long) 10000);
		
		System.out.println(result);
		
		fail("Not yet implemented");
	}

}
