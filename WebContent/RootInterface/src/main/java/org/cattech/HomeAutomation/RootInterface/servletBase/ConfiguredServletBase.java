package org.cattech.HomeAutomation.RootInterface.servletBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.communicationHub.NodeInterfaceString;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.database.Database;
import org.cattech.homeAutomation.homeAutomationContext.HomeAutomationContextListener;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfiguredServletBase {

	private static final long serialVersionUID = 1L;
	private static Logger log = LogManager.getLogger(ConfiguredServletBase.class.getName());
	protected ChannelController controller; 
	
	protected HomeAutomationConfiguration configuration;
	protected HomeAutomationContextListener homeAutoConfig; 
	protected Connection conn = null;
	protected NodeInterfaceString hubInterface = null;

	private Cookie[] cookies;
	
	public void setupServletState(HttpServletRequest request, HttpServletResponse response) {
		
		this.homeAutoConfig = (HomeAutomationContextListener) request.getServletContext().getAttribute(HomeAutomationContextListener.INTERFACE_CONTROLLER);

		controller = homeAutoConfig.getChannelController();

		hubInterface = new NodeInterfaceString(controller);

		Database db = new Database(controller.getConfig());
		conn = db.getHomeAutomationDBConnection();
		
		cookies = request.getCookies();
	}
	
    protected JSONObject loadDevice(int deviceID) {

    	PreparedStatement stmt;
		ResultSet rs;
		JSONObject device = null;
		try {

			String query = "SELECT nativeDevice,interfaceType FROM deviceMap " 
			+ " WHERE deviceMap_id = ?";
			
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, deviceID);
			rs = stmt.executeQuery();

			if (rs.next()) {
				try {
					device = new JSONObject();
					device.put("nativeDevice", new JSONObject(rs.getString("nativeDevice")));
					device.put("interfaceType", rs.getString("interfaceType"));
				} catch (JSONException je) {
					log.error("Error loading device mappings", je);
				}
				if ( rs.next() ) {
					log.error("Error, found more than one device for deviceMap_id = "+deviceID);
				}
			}
		} catch (SQLException e) {
			log.error("Error occured while loading device mappings from datbase", e);
			e.printStackTrace();
		}
    		
    	return device;
	}
	
	public String getModuleChannelName() {
		return "ConfiguredServletBase";
	}
}
