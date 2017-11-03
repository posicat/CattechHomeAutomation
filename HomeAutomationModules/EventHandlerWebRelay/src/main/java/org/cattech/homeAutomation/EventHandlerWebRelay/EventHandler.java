package org.cattech.homeAutomation.EventHandlerWebRelay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

public class EventHandler extends HomeAutomationModule {
	private String urlPrefix;
	public EventHandler(ChannelController controller) {
			super(controller);
			
			final String baseUrl = controller.getConfig().getBaseURL();
			this.urlPrefix = baseUrl+"eventHandler.cgi";
			log.info("Enabling webrelay to " + this.urlPrefix);
		}

	@Override
	public void run() {
		InputStream is = null;
		running=true;
		while (running) {
			try {
				String event = hubInterface.getDataFromController();
				if (null == event) {
					Thread.sleep(100);
				} else {
					boolean handledInternally = false;

					
					
					if (!handledInternally) {
						log.info("Forwarded to web eventHandler: " + event);
						URL url = new URL(urlPrefix + "?event=" + URLEncoder.encode(event, "UTF-8"));
						is = url.openStream();
						BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
						String line;
						while ((line = reader.readLine()) != null) {
							log.info("Response:" + line);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (null != is) {
						is.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public String getModuleChannelName() {
		return "EventHandler";
	}

	@Override
	protected void processMessage(HomeAutomationPacket hap) {
		// TODO Auto-generated method stub
		
	}

}
