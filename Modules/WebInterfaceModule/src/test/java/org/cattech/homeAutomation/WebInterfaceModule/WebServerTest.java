package org.cattech.homeAutomation.WebInterfaceModule;

import org.cattech.homeAutomation.moduleBaseTest.BaseTestForModules;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WebServerTest extends BaseTestForModules{

	private WebServer webServer;
	
	@Override
	@Before
	public void setUp() throws Exception  {
		super.setUp();
		
		controller.getConfig().loadConfiguration();
		
		controller.getConfig().getProps().put(WebServer.CONFIG_WEBSERVER_WAR_FOLDER, "../../HomeAutomationWars/target/");

		webServer = new WebServer(controller);
		new Thread(webServer, "Testing WebServer").start();

		registerChannel(testInterface, new String[] { "testEventHandler" });
	}

	@Override
	@After
	public void tearDown() throws Exception {
		webServer.setRunning(false);
	}
	
//	@Ignore
	@Test
	public void manualTest() throws InterruptedException {
		while(webServer.isRunning()) {
			Thread.sleep(1000);
		}
	}

}
