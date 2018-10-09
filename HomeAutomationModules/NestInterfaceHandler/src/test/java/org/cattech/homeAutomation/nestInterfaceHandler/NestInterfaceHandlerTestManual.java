package org.cattech.homeAutomation.nestInterfaceHandler;

import java.io.File;
import java.util.Properties;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NestInterfaceHandlerTestManual extends BaseTestForModules {
	
	NestInterfaceHandler nestHandler;
	private Properties props;
	File testFolder;


	@Override
	@Before
	public void setUp() throws Exception {
		controller = new ChannelController(new HomeAutomationConfiguration(true));

		testInterface = new NodeInterfaceString(controller);
		registerChannel(testInterface, new String[] { TESTCHANNEL });
		
		
		testFolder = File.createTempFile("NestInterFaceHandlerTestManual", testPacketSource);
		
		props = controller.getConfig().getProps();

		nestHandler = new NestInterfaceHandler(controller);
		new Thread(nestHandler, "Testing NestInterfaceHandler").start();

	}

	@Override
	@After
	public void tearDown() {
		nestHandler.setRunning(false);
	}

	
}
