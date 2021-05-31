package org.cattech.homeAutomation.nestInterfaceHandler;

import java.io.File;
import java.util.Properties;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.junit.After;
import org.junit.Before;

public class GoogleHomeGraphInterfaceHandlerTests extends BaseTestForModules {
	
	GoogleHomeGraphInterfaceHandler homeGraph;
	private Properties props;
	File testFolder;


	@Override
	@Before
	public void setUp() throws Exception {
		controller = new ChannelController(new HomeAutomationConfiguration(true, false));

		testInterface = new NodeInterfaceString(controller);
		registerChannel(testInterface, new String[] { TESTCHANNEL });
		
		
		testFolder = File.createTempFile("NestInterFaceHandlerTestManual", testPacketSource);
		
		props = controller.getConfig().getProps();

		homeGraph = new GoogleHomeGraphInterfaceHandler(controller);
		new Thread(homeGraph, "Testing NestInterfaceHandler").start();

	}

	@Override
	@After
	public void tearDown() {
		homeGraph.setRunning(false);
	}
	
}
