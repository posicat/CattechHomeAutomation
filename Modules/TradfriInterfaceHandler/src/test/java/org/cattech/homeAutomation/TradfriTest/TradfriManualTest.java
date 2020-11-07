package org.cattech.homeAutomation.TradfriTest;

import org.cattech.HomeAutomation.TradfriInterfaceHandler.TradfriInterfaceHandler;
import org.cattech.homeAutomation.configuration.HomeAutomationConfigurationException;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TradfriManualTest extends BaseTestForModules {
	TradfriInterfaceHandler tradfreeHandler;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		
		controller.getConfig().loadConfiguration();

		tradfreeHandler = new TradfriInterfaceHandler(controller);
		new Thread(tradfreeHandler, "Testing DeviceResolver").start();

		registerChannel(testInterface, new String[] { "testEventHandler" });
	}

	@Override
	@After
	public void tearDown() throws Exception {
		tradfreeHandler.setRunning(false);
	}

//	@Ignore
	@Test
	public void testCanTalkToLocalTradfriHub() throws HomeAutomationConfigurationException, InterruptedException {
		boolean onOff = false;
		String[] colorTemps  = {"153","370","454","370"};
		int colIdx = 0;
		while(tradfreeHandler.isRunning()) {
			onOff= ! onOff;
			if (onOff) {
				colIdx++;
				if (colIdx >= colorTemps.length) {
					colIdx=0;
				}
			}
			HomeAutomationPacket hap = new HomeAutomationPacket(
					"{\"destination\":[\"TradfriInterfaceHandler\"]," + testPacketSource + ","
							+ "\"data\":{"
									+"\"nativeDevice\":{\"protocol\":\"tradfri\",\"controlChannel\":\"TradfriInterfaceHandler\",\"name\":\"6w Spot\",\"ID\":65538,\"type\":\"Light\"}"
									+",\"action\":{\"on\":\""+onOff+"\",\"colourTemperature\":\""+colorTemps[colIdx]+"\"}"
							+"}"
					+"}");
			testInterface.sendDataPacketToController(hap);
			Thread.sleep(2000);
		}

	}

}
