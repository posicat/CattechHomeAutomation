package org.cattech.HomeAutomation.servlets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cattech.HomeAutomation.common.database.Database;
import org.cattech.homeAutomation.common.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.common.configuration.HomeAutomationConfigurationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeAutoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Integer selectedGroup;
	private Cookie[] cookies;
	Connection conn = null;
	JSONArray groups = new JSONArray();
	JSONObject rootGroup = null;
	JSONObject currentGroup = null;

	static Logger log = Logger.getLogger(HomeAutoServlet.class.getName());

	public HomeAutoServlet() {
		HomeAutomationConfiguration configuration = null;
		try {
			configuration = new HomeAutomationConfiguration(true, false);
		} catch (HomeAutomationConfigurationException e) {
			log.error("Could not load configuration", e);
		}
		Database db = new Database(configuration);
		conn = db.getHomeAutomationDBConnection();
		
	}

	public void loadCurrentState(HttpServletRequest request, HttpServletResponse response) {
		// If an invalid number is passed in, load the root.
		try {
			selectedGroup = Integer.valueOf(request.getParameter("group"));
		} finally {
			loadGroups();
		}

		cookies = request.getCookies();
	}

	public void loadGroups() {
		PreparedStatement  stmt;
		ResultSet rs;

		try {
			
			String query = "SELECT deviceMap_id,deviceName,nativeDevice FROM deviceMap "
					+ " WHERE interfaceType = 'group'";
			stmt = conn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				JSONObject readGroup = new JSONObject();
				readGroup.put("deviceMap_id", rs.getString("deviceMap_id"));
				readGroup.put("deviceName", rs.getString("deviceName"));
				JSONObject nativeDevice = new JSONObject(rs.getString("nativeDevice")); 
				readGroup.put("nativeDevice", nativeDevice);
				
				if (nativeDevice.getInt("parent") == -1) {
					rootGroup = readGroup;
					if (selectedGroup == null) { // If we have no selected group via the webpage, select the default
						selectedGroup = rs.getInt("deviceMap_id");
					}
				}
				if (selectedGroup != null) {
					if (nativeDevice.getInt("group") == selectedGroup) {
						currentGroup = readGroup;
					}
				}
				
				groups.put(readGroup);
			}
		} catch (SQLException e) {
			log.error("Error occured while loading group mappings from datbase", e);
			e.printStackTrace();
		}
	}

	public String getJSONListOfDevicesForCurrentGroup() {
		PreparedStatement  stmt;
		ResultSet rs;
		JSONArray deviceList = new JSONArray();
		try {
			
			String query = "SELECT interfaceType,deviceName,dm.deviceMap_id,x,dx,y,dy FROM deviceMap dm "
					+ " LEFT JOIN menuControl mc ON mc.deviceMap_id = dm.deviceMap_id "
					+ " WHERE mc.menuGrpID = ?";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, selectedGroup);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				try {
					JSONObject device = new JSONObject();
					device.put("interfaceType", rs.getString("interfaceType"));
					device.put("deviceName", rs.getString("deviceName"));
					device.put("deviceMap_id", rs.getString("deviceMap_id"));
					device.put("x", rs.getString("x"));
					device.put("y", rs.getString("y"));
					device.put("dx", rs.getString("dx"));
					device.put("dy", rs.getString("dy"));
					deviceList.put(device);
				} catch (JSONException je) {
					log.error("Error loading device mappings", je);
				}
			}
		} catch (SQLException e) {
			log.error("Error occured while loading device mappings from datbase", e);
			e.printStackTrace();
		}
		
		return deviceList.toString();
	}
	
	public String getGroupName() {
		return currentGroup.getString("deviceName");
	}
}
