package powerlineMonitor;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.rainforestEMU2.commandLine.RainforestEMU2;
import org.cattech.rainforestEMU2.serialInterface.SerialRainforestCommunications;
import org.cattech.rainforestEMU2.xmlCommunications.RainforestCommunicationsInterface;
import org.cattech.rainforestEMU2.xmlCommunications.RainforestTranslate;
import org.json.JSONObject;
import org.xml.sax.SAXException;

public class PowerlineMonitor extends HomeAutomationModule {

	private static final String RAINFOREST_EMU_PORT = "rainforestEmuPort";

	private static SerialRainforestCommunications serialCommunication;
	static RainforestCommunicationsInterface callback;
	static Logger log = Logger.getLogger(PowerlineMonitor.class.getName());
	static String port = null;
	static String rainforestMAC;

	public PowerlineMonitor(ChannelController controller) {
		super(controller);
	}

	@Override
	public void run() {
		this.running = true;

		try {
			port = configuration.getProps().getProperty(RAINFOREST_EMU_PORT);
			serialCommunication = new SerialRainforestCommunications(port, new RainforestEMU2Callback());
			serialCommunication.run();

			serialCommunication.clearSchedule();
			serialCommunication.setSchedule(null, "demand", new Integer(10), Boolean.TRUE, "rest");
			serialCommunication.setSchedule(null, "price", new Integer(3600), Boolean.TRUE, "rest");
			serialCommunication.getSchedule(null, null, "rest");

			while (serialCommunication.isRunning()) {
				synchronized (serialCommunication) {
					serialCommunication.wait(100);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			callback.onShutdown(e);
		} finally {
			serialCommunication.shutDown();
		}
	}

	@Override
	public String getModuleChannelName() {
		return "powerlineMonitor";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (!incoming.hasWrapper("data")) {
			log.error("Packet has no data element. " + incoming);
		} else {
		}
	}

	static class RainforestEMU2Callback implements RainforestCommunicationsInterface {

		private static final String POWER_LINE_MONITOR = "PowerLineMonitor";
		static boolean grabMacineInformation = true;

		public RainforestEMU2Callback() {
		}

		public void readReplyXML(String xmlData) {
//			log.debug("Read:\n" + xmlData.replaceAll("[\\r\\n ]", ""));

			// Processing the XML data into something a little bit more useful.
			String json = "";
			try {
				json = RainforestTranslate.toHumanReadableJson(xmlData, grabMacineInformation).toString();
			} catch (SAXException | IOException | ParserConfigurationException e) {
				log.error("Couldn't parse XML :\n" + xmlData, e);
				json = "{'error':'" + e.getMessage() + "'}";
			} catch (Exception e) {
				log.error("Something went wrong while parsing the XML", e);
			}
//			log.debug(grabMacineInformation + "Converted:\n" +  json);

			if (grabMacineInformation) {
				// First pass grab the MAC address, otherwise hide it to send on to the server
				log.info("Initial packet : " + json);
				try {
					JSONObject readJson = new JSONObject(json);
					for (String name : JSONObject.getNames(readJson)) {
						JSONObject jso = readJson.getJSONObject(name);
						if (null != jso && jso.has("MeterMacId")) {
							rainforestMAC = jso.getString("MeterMacId");
							break;
						}
					}

					if (null != rainforestMAC) {
						grabMacineInformation = false;
					}
				} catch (Exception e) {
					log.error("Broke!", e);
				}
			} else {
				HomeAutomationPacket reply = new HomeAutomationPacket();
				reply.setSource(POWER_LINE_MONITOR);
				reply.setDestination(HomeAutomationPacket.CHANNEL_EVENT_HANDLER);
				reply.setData(new JSONObject(json));
				JSONObject nativeDevice = new JSONObject();
				nativeDevice.put("meter", rainforestMAC);
				nativeDevice.put("protocol", "Rainforest");
				reply.putData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE, nativeDevice);
				hubInterface.sendDataPacketToController(reply);
			}
		}

		public void onShutdown(Exception e) {
			if (null != e) {
				log.debug("Bailing on error : " + e.getMessage());
				e.printStackTrace();
			}
			serialCommunication.shutDown();
			// We die, any last requests?
		}

		public void onNonFatalException(Exception e) {
			e.printStackTrace();
		}

	}

}
