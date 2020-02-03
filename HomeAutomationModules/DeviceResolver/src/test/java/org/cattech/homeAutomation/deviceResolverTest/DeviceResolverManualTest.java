package org.cattech.homeAutomation.deviceResolverTest;

import org.cattech.homeAutomation.configuration.HomeAutomationConfigurationException;
import org.cattech.homeAutomation.deviceResolver.DeviceResolver;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DeviceResolverManualTest extends BaseTestForModules {
	DeviceResolver deviceResolver;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		deviceResolver = new DeviceResolver(controller);
		new Thread(deviceResolver, "Testing DeviceResolver").start();

		registerChannel(testInterface, new String[] { "testEventHandler" });
	}

	@After
	public void tearDown() throws Exception {
		deviceResolver.setRunning(false);
	}

	@Test
	public void testCanTalkToLocalMySQL() throws HomeAutomationConfigurationException {
		controller.getConfig().getProps().setProperty("homeAutomation.config", "/etc/homeAutomation");
		controller.getConfig().loadConfiguration();

		deviceResolver.loadDeviceMappings();
	}

}
