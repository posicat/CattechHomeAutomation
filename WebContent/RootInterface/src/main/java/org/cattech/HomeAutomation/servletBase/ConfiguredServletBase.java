package org.cattech.HomeAutomation.servletBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cattech.HomeAutomation.database.Database;
import org.cattech.HomeAutomation.homeAutomationContext.HomeAutomationContextListener;
import org.cattech.HomeAutomation.homeAutomationContext.HomeAutomationControllerRunnable;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfiguredServletBase {

	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(ConfiguredServletBase.class.getName());

	
	protected HomeAutomationConfiguration configuration;
	protected Connection conn = null;

	private Cookie[] cookies;
	
	public void setupServletState(HttpServletRequest request, HttpServletResponse response) {
		HomeAutomationControllerRunnable homeAutoConfig = (HomeAutomationControllerRunnable) 
				request.getServletContext().getAttribute(HomeAutomationContextListener.INTERFACE_CONTROLLER);
		
		configuration = homeAutoConfig.getConfiguration();
		
		Database db = new Database(configuration);
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
					device.put("nativeDevice", rs.getString("nativeDevice"));
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
	

}
