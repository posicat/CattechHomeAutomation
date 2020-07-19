package org.cattech.homeAutomation.nestInterfaceHandler;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.configuration.HomeAutomationConfigurationException;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class NestInterfaceHandler extends HomeAutomationModule {
	static Logger log = Logger.getLogger(NestInterfaceHandler.class.getName());

	private static final int WAIT_BETWEEN_NEST_POLING = 1 * 60 * 1000;
	private static final int WAIT_WHEN_BLOCKED = 10 * 60 * 1000;

	private static String URL_DEVICES = "https://developer-api.nest.com/devices/";
	private static String URL_STRUCTURES = "https://developer-api.nest.com/structures/";
	private static String URL_DEVICES_THERMOSTATS = "https://developer-api.nest.com/devices/thermostats";
	private static String URL_OAUTH2_ACCESS_TOKEN = "https://api.home.nest.com/oauth2/access_token";

	JSONObject authData = new JSONObject();
	// Page to get PIN code : – https://developer.nest.com/ then Authorization URL
	// URL to get PIN : https://home.nest.com/login/oauth2?client_id={your client
	// id}&state=STATE

	private File authCache;

	public NestInterfaceHandler(ChannelController controller) {
		super(controller);
		authCache = new File(configuration.getLogFolder() + "authenticationCache.json");
		try {
			authData = new JSONObject(loadFile(authCache));
		} catch (Exception e) {
			log.error("Could not load authentication cache.  Continuing.", e);
		}
	}

	@Override
	public boolean autoStartModule() {
		log.debug("Testing override autostart");
		return true;
	}

	@Override
	public void run() {
		this.running = autoStartModule();
		if (!this.running) {
			return;
		}

		while (this.running) {
			if (!authData.has("access_token")) {
				try {
					authenticateWithNestAccount();
				} catch (HomeAutomationConfigurationException e) {
					log.error("Suhtting down Nest Interface Handler", e);
					this.running = false;
				}
			}
			try {
				gatherAndPackageNestData();
			} catch (JSONException | IOException e) {
				log.error("Could not gather data to log", e);
			}

			sleepNoThrowWhileWatchingForIncomingPackets(WAIT_BETWEEN_NEST_POLING);
		}
	}

	protected void gatherAndPackageNestData() throws JSONException, IOException {
		log.debug("Retrieving Nest data ...");
		JSONObject devices = callNestAPIWithData(URL_DEVICES, "auth=" + authData.getString("access_token"));

		if (devices.has("thermostats")) {

			JSONObject thermostatData = devices.getJSONObject("thermostats");

			String[] thermostats = JSONObject.getNames(thermostatData);

			for (int i = 0; i < thermostats.length; i++) {
				String tstat = thermostats[i];

				JSONObject thermostat = callNestAPIWithData(URL_DEVICES_THERMOSTATS + "/" + tstat + "/", "auth=" + authData.getString("access_token"));

				System.out.println(thermostat.toString());

				HomeAutomationPacket sensorPacket = new HomeAutomationPacket();

				sensorPacket.putWrapper("source", "nestInterfaceHandler");

				sensorPacket.putData(tstat, thermostat);

				HomeAutomationPacket readPacket = new HomeAutomationPacket();
				readPacket.setSource(this.getModuleChannelName());
				readPacket.setDestination(HomeAutomationPacket.CHANNEL_EVENT_HANDLER);
				readPacket.setData(thermostat);
				JSONObject nativeDevice = new JSONObject();
				nativeDevice.put("thermostat", tstat);
				nativeDevice.put("protocol", "NestAPI");
				readPacket.putData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE, nativeDevice);
				log.debug(readPacket);
				hubInterface.sendDataPacketToController(readPacket);
			}
		}
	}

	@Override
	public String getModuleChannelName() {
		return "NestInterfaceHandler";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket arg0, List<HomeAutomationPacket> arg1) {
		// TODO Auto-generated method stub

	}

	private void authenticateWithNestAccount() throws HomeAutomationConfigurationException {
		try {
			Properties props = configuration.getProps();
			String productID = props.getProperty("nest.product_id");
			String pinCode = props.getProperty("nest.pin_code");
			String clientSecret = props.getProperty("nest.product_secret");

			StringBuilder authURL = new StringBuilder();
			authURL.append("code=").append(pinCode);
			authURL.append("&client_id=").append(productID);
			authURL.append("&client_secret=").append(clientSecret);
			authURL.append("&grant_type=authorization_code");

			authData = callNestAPIWithData(URL_OAUTH2_ACCESS_TOKEN, authURL.toString());

			if (authData.has("access_token")) {
				writeFile(authCache, authData.toString());
			} else {
				log.error("We did not successfully retrieve the access key." + authData.toString());
			}
		} catch (Exception e) {
			throw new HomeAutomationConfigurationException("Could not authenticate with Nest server", e);
		}
	}

	private JSONObject callNestAPIWithData(String url, String params) throws IOException {
		OkHttpClient client = new OkHttpClient();
		MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
		RequestBody body = RequestBody.create(mediaType, params);
		Request request = new Request.Builder()
				.url(url + "?" + params)
				// .post(body)
				.build();
		Response response = null;
		response = client.newCall(request).execute();

		String authJSON = response.body().string();
		JSONObject j = new JSONObject(authJSON);

		if (j.has("blocked")) {
			log.error("Going to fast for the Nest servers, waiting for a bit.");
			sleepNoThrowWhileWatchingForIncomingPackets(WAIT_WHEN_BLOCKED);
		}
		if (j.has("error")) {
			log.error("Error returned from Nest API expiring authentication cache. : " + (j.has("error_description") ? j.getString("error_description") : ""));
			authData = new JSONObject();
		}
		return j;
	}

	public static void main(String[] args) throws IOException, HomeAutomationConfigurationException {
		ChannelController controller = new ChannelController(new HomeAutomationConfiguration());
		NestInterfaceHandler nih = new NestInterfaceHandler(controller);

		System.out.println(nih.authData);

		nih.gatherAndPackageNestData();
	}


}
