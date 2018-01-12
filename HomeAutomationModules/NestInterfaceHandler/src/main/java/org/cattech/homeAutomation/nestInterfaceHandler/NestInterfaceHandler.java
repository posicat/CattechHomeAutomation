package org.cattech.homeAutomation.nestInterfaceHandler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONObject;

public class NestInterfaceHandler extends HomeAutomationModule {
	static Logger log = Logger.getLogger(NestInterfaceHandler.class.getName());

	private static final int WAIT_BETWEEN_NEST_POLING = 15 * 60 * 1000;
	private static String URL_DEVICES_THERMOSTATS = "https://developer-api.nest.com/devices/thermostats";
	private static String URL_OAUTH2_ACCESS_TOKEN = "https://api.home.nest.com/oauth2/access_token";

	JSONObject authData = new JSONObject();
	// Page to get PIN code : – https://developer.nest.com/ then Authorization URL

	// #nestset() {
	// #curl -L -X PUT
	// "https://developer-api.nest.com/devices/thermostats/$YOUR_DEVICE_ID/target_temperature_f?auth=$YOUR_ACCESS_TOKEN$"
	// -H "Content-Type: application/json" -d "$1"
	// #nestget() {
	// #curl -L
	// https://developer-api.nest.com/devices/thermostats/$YOUR_DEVICE_ID/target_temperature_f\?auth\=$YOUR_ACCESS_TOKEN$
	// #nestaway() {
	// #curl -L -X PUT
	// "https://developer-api.nest.com/structures/$YOUR_STRUCTURE_ID/away?auth=$YOUR_ACCESS_TOKEN$"
	// -H "Content-Type: application/json" -d '"away"'
	// #nesthome() {
	// #curl -L -X PUT
	// "https://developer-api.nest.com/structures/$YOUR_STRUCTURE_ID/away?auth=$YOUR_ACCESS_TOKEN$"
	// -H "Content-Type: application/json" -d '"home"'

	public NestInterfaceHandler(ChannelController controller) {
		super(controller);
	}

	@Override
	public void run() {
		this.running = autoStartModule();
		if (! this.running) {return;}

		loadNestAPIProductCodes();

		while (this.running) {
			sleepNoThrowWhileWatchingForIncomingPackets(WAIT_BETWEEN_NEST_POLING);

			log.debug("Retrieving Nest data ...");

			JSONObject deviceData = getNestDataForDevices();

			HomeAutomationPacket sensorPacket = new HomeAutomationPacket();

			// For each device found in the data:

			log.debug("sensorPacket : \r\n" + sensorPacket);

			sensorPacket.addDestination("sensorLogging");
			sensorPacket.getWrapper().put("source", "nestInterfaceHandler");
			String tempValue = null;
			String humidValue = null;
			sensorPacket.getData().put("temp", tempValue);
			sensorPacket.getData().put("humid", humidValue);
		}
	}

	@Override
	public String getModuleChannelName() {
		// TODO Auto-generated method stub
		return "NestInterfaceHandler";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket arg0, List<HomeAutomationPacket> arg1) {
		// TODO Auto-generated method stub

	}

	protected JSONObject getNestDataForDevices() {
		String accessToken = configuration.getProps().getProperty("nest.access_token");

		String devicesURL = URL_DEVICES_THERMOSTATS + "?auth=" + accessToken;

		String deviceJson = null;
		try {
			deviceJson = getDataFromURL(devicesURL);
		} catch (IOException e) {
			log.error("Could not load nest device data from API", e);
		}
		JSONObject devices = new JSONObject(deviceJson);

		return devices;
	}

	void loadNestAPIProductCodes() {
		boolean retrieveAuthentication = false;

		Properties props = configuration.getProps();
		File authCache = new File(configuration.getLogFolder() + "authenticationCache.json");

		if (authCache.exists()) {
			try {
				authData = new JSONObject(loadFile(authCache));
			} catch (Exception e) {
				log.error("Error loading cached Nest data", e);
			}
		} else {
			retrieveAuthentication = true;
		}

		if (retrieveAuthentication) {
			// #"https://api.home.nest.com/oauth2/access_token?
			// #client_id=%YOUR_PRODUCT_ID%
			// #&amp;code=%YOUR_PIN_CODE%
			// #&amp;#client_secret=%YOUR_PRODUCT_SECRET%
			// #&amp;grant_type=authorization_code"

			String productID = props.getProperty("nest.product_id");
			String pinCode = props.getProperty("nest.pin_code");
			String clientSecret = props.getProperty("nest.product_secret");

			StringBuilder authURL = new StringBuilder(URL_OAUTH2_ACCESS_TOKEN);
			authURL.append("?client_id=").append(productID);
			authURL.append("&code=").append(pinCode);
			authURL.append("&client_secret=").append(clientSecret);
			authURL.append("&grant_type=authorizationCode");

			String authJSON;
			try {
				authJSON = getDataFromURL(authURL.toString());
				writeFile(authCache, authJSON);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	// #================================================================================
	// sub writeSensorData {
	// my ($data,$node)=@_;
	//
	// my $sensors = {};
	//
	// $sensors->{ambient_temperature_c}='temp,1';
	// $sensors->{humidity}='humid,2';
	//
	// foreach my $k (keys %$sensors) {
	//
	// my ($name,$subNode)=split(',',$sensors->{$k});
	//
	// my $sensor_data={
	// 'node'=>$node,
	// 'name'=>$name,
	// 'value'=>$data->{$k},
	// 'subNode'=>int($subNode),
	// };
	//
	// # print Dumper $sensor_data;
	//
	// if (defined $sensor_data->{value} ) {
	// $sensorNode->addupdate_data($sensor_data,'sensorData');
	// }
	// }
	// }

}
