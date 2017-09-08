package org.cattech.homeAutomation.channelHandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;

public class EventHandlerWebRelay implements Runnable {

		private String urlPrefix;
		private NodeInterfaceString hubInterface;
		private boolean running = true; 

		public EventHandlerWebRelay(ChannelController controller, String urlPrefix) {
			this.urlPrefix = urlPrefix;
			hubInterface = new NodeInterfaceString(controller);
		}
	
	@Override
	public void run() {
		System.out.println("Enabling webrelay to " + urlPrefix);
		
		hubInterface.sendDataToController("{\"register\":[\"eventHandler\"]}");
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		InputStream is = null;
		
		String response = null;
		while (null==response) {
			response = hubInterface.getDataFromController();
		}
//		System.out.println(response);
		
		while (running ) {
			try {
				String event = hubInterface.getDataFromController();
				if (null != event ) {
					System.out.println("Forwarded to web eventHandler: "+ event);
					URL url = new URL(urlPrefix+URLEncoder.encode(event, "UTF-8"));
					is = url.openStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					String line; 
					while ( (line = reader.readLine()) != null) {
						System.out.println(line);
					}
				}else{
					Thread.sleep(100);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (null!=is) {
						is.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
}
