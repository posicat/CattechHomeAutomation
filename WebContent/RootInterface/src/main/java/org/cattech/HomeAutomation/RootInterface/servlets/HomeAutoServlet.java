package org.cattech.HomeAutomation.RootInterface.servlets;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.HomeAutomation.RootInterface.servletBase.ConfiguredServletBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeAutoServlet extends ConfiguredServletBase {

	private static final long serialVersionUID = 1L;
	static Logger log = LogManager.getLogger(HomeAutoServlet.class.getName());
	
	private Integer selectedMenuPage;
	JSONArray menuPages = new JSONArray();
	JSONObject rootMenuPage = null;
	JSONObject currentPage = null;

	@Override
	public void setupServletState(HttpServletRequest request, HttpServletResponse response) {

		super.setupServletState(request, response);

		String menuPage = request.getParameter("menuPage");
		if (menuPage != null) {
			try {
				selectedMenuPage = Integer.valueOf(menuPage);
			} catch (NumberFormatException e) {
				// If an invalid number is passed in, ignore it, we'll load the default. But log
				// it just for giggles.
				log.info("Value passed in menuPage parameter was invalid : " + menuPage);
			}
		}

		menuPages();
	}

	public void menuPages() {
		PreparedStatement stmt;
		ResultSet rs;

		try {

			String query = "SELECT deviceMap_id,deviceName,nativeDevice FROM deviceMap " + " WHERE interfaceType = 'menuPage'";
			stmt = conn.prepareStatement(query);
			rs = stmt.executeQuery();

			while (rs.next()) {
				JSONObject readPage = new JSONObject();
				readPage.put("deviceMap_id", rs.getString("deviceMap_id"));
				readPage.put("deviceName", rs.getString("deviceName"));
				JSONObject nativeDevice = new JSONObject(rs.getString("nativeDevice"));
				readPage.put("nativeDevice", nativeDevice);

				if (nativeDevice.getInt("parent") == -1) {
					rootMenuPage = readPage;
					if (selectedMenuPage == null) { // If we have no selected menuPage via the webpage, select the default
						selectedMenuPage = rs.getInt("deviceMap_id");
					}
				}
				if (selectedMenuPage != null) {
					if (nativeDevice.getInt("menuPage") == selectedMenuPage) {
						currentPage = readPage;
					}
				}

				menuPages.put(readPage);
			}
		} catch (SQLException e) {
			log.error("Error occured while loading menuPage mappings from datbase", e);
			e.printStackTrace();
		}
	}

	public String getJSONListOfDevicesForCurrentMenuPage() {
		PreparedStatement stmt;
		ResultSet rs;
		JSONArray deviceList = new JSONArray();
		try {

			String query = "SELECT interfaceType,deviceName,dm.deviceMap_id,x,dx,y,dy,image FROM deviceMap dm " + " LEFT JOIN menuControl mc ON mc.deviceMap_id = dm.deviceMap_id " + " WHERE mc.menuGrpID = ?";
			stmt = conn.prepareStatement(query);
			stmt.setInt(1, selectedMenuPage);
			rs = stmt.executeQuery();

			while (rs.next()) {
				try {
					JSONObject device = new JSONObject();
					device.put("interfaceType", rs.getString("interfaceType"));
					device.put("deviceName", rs.getString("deviceName"));
					device.put("deviceMap_id", rs.getString("deviceMap_id"));
					device.put("image", rs.getString("image"));
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

	public String getMenuPage() {
		return currentPage.getString("deviceName");
	}
}
