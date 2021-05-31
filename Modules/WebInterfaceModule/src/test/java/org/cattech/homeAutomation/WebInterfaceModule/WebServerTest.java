package org.cattech.homeAutomation.WebInterfaceModule;

import java.util.HashMap;

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
		
		controller.getConfig().getProps().put(WebServer.CONFIG_WEBSERVER_WAR_FOLDER, "../../WebContent/target/");

		HashMap<String, String> manifest = new HashMap<String, String>();
		manifest.put("Implementation-Title", "WebServerTest");
		manifest.put("Implementation-Version", "manual_test");

		webServer = new WebServer(controller);
		webServer.setManifest(manifest);

		new Thread(webServer, "Testing WebServer").start();
		
		registerChannel(testInterface, new String[] { "testEventHandler" });
		
		while(webServer.isRunning()) {
			sleepNoThrow(1000);
		}
	}
	
	public static void sleepNoThrow(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e1) {
			// We don't care, just go on.
		}
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
