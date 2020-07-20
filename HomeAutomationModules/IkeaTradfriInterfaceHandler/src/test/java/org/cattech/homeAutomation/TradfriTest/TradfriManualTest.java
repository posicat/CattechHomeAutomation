package org.cattech.homeAutomation.TradfriTest;

import org.cattech.HomeAutomation.TradfriInterfaceHandler.TradfriInterfaceHandler;
import org.cattech.homeAutomation.configuration.HomeAutomationConfigurationException;
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

		tradfreeHandler = new TradfriInterfaceHandler(controller);
		new Thread(tradfreeHandler, "Testing DeviceResolver").start();

		registerChannel(testInterface, new String[] { "testEventHandler" });
	}

	@Override
	@After
	public void tearDown() throws Exception {
		tradfreeHandler.setRunning(false);
	}

	@Test
	public void testCanTalkToLocalTradfriHub() throws HomeAutomationConfigurationException, InterruptedException {
		while(tradfreeHandler.isRunning()) {
			Thread.sleep(1000);
		}

	}

}
