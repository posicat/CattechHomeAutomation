package org.cattech.homeAutomation.nestInterfaceHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.configuration.HomeAutomationConfigurationException;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.smartdevicemanagement.v1.SmartDeviceManagementScopes;

public class GoogleHomeGraphInterfaceHandler extends HomeAutomationModule {
	static Logger log = LogManager.getLogger(GoogleHomeGraphInterfaceHandler.class.getName());
	
	// Components for authentication flow
	GoogleAuthorizationCodeFlow flow;

	private Credential crediential;
	
	private static final File DATA_STORE_DIR = new File("/var/run/CattechHomeAutomation/GoogleHomeGraphInterfaceHandler");

	private static final String CLIENT_SECRET = "{mnuSnrRkXEUITlM5NIfVp92e}";
	private static final String CLIENT_ID = "223356369013-f7co5k3ld5gtelpq4802gfddghmgh825.apps.googleusercontent.com";
	
	private final static String CLIENT_SECRETS = "{\"installed\": {\"client_id\": \""+CLIENT_ID+"\",\"client_secret\": \"" + CLIENT_SECRET + "\"}}";
	
	
    // Our fields
	private static final int WAIT_BETWEEN_DEVICE_POLING = 10000;
			
	public GoogleHomeGraphInterfaceHandler(ChannelController controller) {
		super(controller);

		HomeAutomationConfiguration configuration = controller.getConfig();

		Collection<String> scopes = new ArrayList<String>();
		scopes.add(SmartDeviceManagementScopes.SDM_SERVICE);

		//TODO Expand this to create a unique GUID in the DB instead
		String localUniqueId = "CattechHomeAutomation@"+ configuration.getHost();

		try {
			
			FileDataStoreFactory DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
	
			// Build flow and trigger user authorization request.
			flow = new GoogleAuthorizationCodeFlow.Builder(
					new NetHttpTransport(), 
					new JacksonFactory(),
					CLIENT_ID,
					CLIENT_SECRET,
					scopes
			).build();
			
			while (null == crediential) {
				crediential = flow.loadCredential(localUniqueId);
			
				if (null == crediential) {
						GoogleAuthorizationCodeRequestUrl newAuthorizationUrl = flow.newAuthorizationUrl();
						newAuthorizationUrl.setRedirectUri("urn:ietf:wg:oauth:2.0:oob");
						
						log.info("Must hit this url to authenticate : " + newAuthorizationUrl);
						sleepNoThrow(10000);
				}
			}
		} catch (IOException e) {
			log.error("Could not initialize GoogleAuthorizationCodeFlow",e);
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

			gatherAndPackageNestData();

			sleepNoThrowWhileWatchingForIncomingPackets(WAIT_BETWEEN_DEVICE_POLING);
		}
	}

	protected void gatherAndPackageNestData()  {
		log.debug("Retrieving Nest data ...");
		
//		
//		
//		
//		JSONObject devices = callNestAPIWithData(URL_DEVICES, "auth=" + authData.getString("access_token"));
//
//		if (devices.has("thermostats")) {
//
//			JSONObject thermostatData = devices.getJSONObject("thermostats");
//
//			String[] thermostats = JSONObject.getNames(thermostatData);
//
//			for (int i = 0; i < thermostats.length; i++) {
//				String tstat = thermostats[i];
//
//				JSONObject thermostat = callNestAPIWithData(URL_DEVICES_THERMOSTATS + "/" + tstat + "/", "auth=" + authData.getString("access_token"));
//
//				System.out.println(thermostat.toString());
//
//				HomeAutomationPacket sensorPacket = new HomeAutomationPacket();
//
//				sensorPacket.putWrapper("source", "nestInterfaceHandler");
//
//				sensorPacket.putData(tstat, thermostat);
//
//				HomeAutomationPacket readPacket = new HomeAutomationPacket();
//				readPacket.setSource(this.getModuleChannelName());
//				readPacket.setDestination(HomeAutomationPacket.CHANNEL_EVENT_HANDLER);
//				readPacket.setData(thermostat);
//				JSONObject nativeDevice = new JSONObject();
//				nativeDevice.put("thermostat", tstat);
//				nativeDevice.put("protocol", "NestAPI");
//				readPacket.putData(HomeAutomationPacket.FIELD_DATA_NATIVE_DEVICE, nativeDevice);
//				log.debug(readPacket);
//				hubInterface.sendDataPacketToController(readPacket);
//			}
//		}
	}

	@Override
	public String getModuleChannelName() {
		return "GoogleSmartDeviceInterfaceHandler";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket arg0, List<HomeAutomationPacket> arg1) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws IOException, HomeAutomationConfigurationException {
		ChannelController controller = new ChannelController(new HomeAutomationConfiguration());
		GoogleHomeGraphInterfaceHandler nih = new GoogleHomeGraphInterfaceHandler(controller);

		nih.gatherAndPackageNestData();
	}


	private String generateJavascriptAuthentication(String apiKey, String clientID, String scope) {
		String jsAuth = "<script>\n" + 
				"  var GoogleAuth;\n" + 
				"  var SCOPE = '"+scope+"';\n" + 
				"  function handleClientLoad() {\n" + 
				"    // Load the API's client and auth2 modules.\n" + 
				"    // Call the initClient function after the modules load.\n" + 
				"    gapi.load('client:auth2', initClient);\n" + 
				"  }\n" + 
				"\n" + 
				"  function initClient() {\n" + 
				"    // In practice, your app can retrieve one or more discovery documents.\n" + 
				"    var discoveryUrl = 'https://www.googleapis.com/discovery/v1/apis/drive/v3/rest';\n" + 
				"\n" + 
				"    // Initialize the gapi.client object, which app uses to make API requests.\n" + 
				"    // Get API key and client ID from API Console.\n" + 
				"    // 'scope' field specifies space-delimited list of access scopes.\n" + 
				"    gapi.client.init({\n" + 
				"        'apiKey': '"+apiKey+"',\n" + 
				"        'clientId': '"+clientID+"',\n" + 
				"        'discoveryDocs': [discoveryUrl],\n" + 
				"        'scope': SCOPE\n" + 
				"    }).then(function () {\n" + 
				"      GoogleAuth = gapi.auth2.getAuthInstance();\n" + 
				"\n" + 
				"      // Listen for sign-in state changes.\n" + 
				"      GoogleAuth.isSignedIn.listen(updateSigninStatus);\n" + 
				"\n" + 
				"      // Handle initial sign-in state. (Determine if user is already signed in.)\n" + 
				"      var user = GoogleAuth.currentUser.get();\n" + 
				"      setSigninStatus();\n" + 
				"\n" + 
				"      // Call handleAuthClick function when user clicks on\n" + 
				"      //      \"Sign In/Authorize\" button.\n" + 
				"      $('#sign-in-or-out-button').click(function() {\n" + 
				"        handleAuthClick();\n" + 
				"      });\n" + 
				"      $('#revoke-access-button').click(function() {\n" + 
				"        revokeAccess();\n" + 
				"      });\n" + 
				"    });\n" + 
				"  }\n" + 
				"\n" + 
				"  function handleAuthClick() {\n" + 
				"    if (GoogleAuth.isSignedIn.get()) {\n" + 
				"      // User is authorized and has clicked \"Sign out\" button.\n" + 
				"      GoogleAuth.signOut();\n" + 
				"    } else {\n" + 
				"      // User is not signed in. Start Google auth flow.\n" + 
				"      GoogleAuth.signIn();\n" + 
				"    }\n" + 
				"  }\n" + 
				"\n" + 
				"  function revokeAccess() {\n" + 
				"    GoogleAuth.disconnect();\n" + 
				"  }\n" + 
				"\n" + 
				"  function setSigninStatus() {\n" + 
				"    var user = GoogleAuth.currentUser.get();\n" + 
				"    var isAuthorized = user.hasGrantedScopes(SCOPE);\n" + 
				"    if (isAuthorized) {\n" + 
				"      $('#sign-in-or-out-button').html('Sign out');\n" + 
				"      $('#revoke-access-button').css('display', 'inline-block');\n" + 
				"      $('#auth-status').html('You are currently signed in and have granted ' +\n" + 
				"          'access to this app.');\n" + 
				"    } else {\n" + 
				"      $('#sign-in-or-out-button').html('Sign In/Authorize');\n" + 
				"      $('#revoke-access-button').css('display', 'none');\n" + 
				"      $('#auth-status').html('You have not authorized this app or you are ' +\n" + 
				"          'signed out.');\n" + 
				"    }\n" + 
				"  }\n" + 
				"\n" + 
				"  function updateSigninStatus() {\n" + 
				"    setSigninStatus();\n" + 
				"  }\n" + 
				"</script>\n" + 
				"\n" + 
				"<button id=\"sign-in-or-out-button\"\n" + 
				"        style=\"margin-left: 25px\">Sign In/Authorize</button>\n" + 
				"<button id=\"revoke-access-button\"\n" + 
				"        style=\"display: none; margin-left: 25px\">Revoke access</button>\n" + 
				"\n" + 
				"<div id=\"auth-status\" style=\"display: inline; padding-left: 25px\"></div><hr>\n" + 
				"\n" + 
				"<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js\"></script>\n" + 
				"<script async defer src=\"https://apis.google.com/js/api.js\"\n" + 
				"        onload=\"this.onload=function(){};handleClientLoad()\"\n" + 
				"        onreadystatechange=\"if (this.readyState === 'complete') this.onload()\">\n" + 
				"</script>";
		
		return jsAuth;
	}
}
