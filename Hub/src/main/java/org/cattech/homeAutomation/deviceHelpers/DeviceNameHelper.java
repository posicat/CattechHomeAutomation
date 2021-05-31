package org.cattech.homeAutomation.deviceHelpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.database.Database;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceNameHelper {
	static Logger log = LogManager.getLogger(DeviceNameHelper.class.getName());

	public static boolean commonDescriptorsMatch(JSONArray fullDescriptor, JSONArray possibleSubDescriptor) {
		int keysMatched = 0;

		for (Object entry : possibleSubDescriptor) {
			List<Object> mightEntries = fullDescriptor.toList();
			if (mightEntries.contains(entry)) {
				keysMatched++;
			}
		}
		// log.debug("Matching " + mightMatch + " <to> " + toMatch +
		// "["+keysMatched+"/"+toMatch.length()+"]");
		return (keysMatched == possibleSubDescriptor.length());
	}

	public static boolean nativeKeysMatch(JSONObject mightMatch, JSONObject toMatch) {
		int keysMismatched = 0;
		for (String key : toMatch.keySet()) {
			String mm = null;
			String tm = null;
			if (!key.equals("controlChannel")) {
				if (mightMatch.has(key)) {
					mm = getKeyAsString(mightMatch,key).toUpperCase();
					if (toMatch.has(key)) {
						tm = getKeyAsString(mightMatch,key).toUpperCase();

						if (!mightMatch.has(key) || !mm.equals(tm)) {
							keysMismatched++;
//							 log.debug("Didn't match " + key + " : " + mm + "::" + tm);
						} else {
//							 log.debug("Matched " + key + " : " + mm + "::" + tm);
						}
					}
				}
			}
		}
		boolean matched = keysMismatched == 0;
		if (matched) {
//			 log.debug("Match " + mightMatch + " <to> " + toMatch + "[" + keysMismatched +
//			 "/" + toMatch.length() + "]");
		} else {
//			 log.debug("Differ " + mightMatch + " <to> " + toMatch + "[" + keysMismatched
//			 + "/" + toMatch.length() + "]");
		}
		return (matched);
	}
	
	private static String getKeyAsString(JSONObject json, String key) {
		Object val = json.get(key);
		return(String.valueOf(val));
	}

	public static void loadDeviceMappings(Database db, Hashtable<JSONObject, JSONArray> lookupTable) {
		
		Connection conn = db.getHomeAutomationDBConnection();
		if (null == conn) {
			log.error("Could not obtain connection to HomeAutomation database");
			return;
		}

		Statement stmt;
		ResultSet rs;
		try {
			stmt = conn.createStatement();
			String query = "SELECT commonDevice,nativeDevice FROM deviceMap";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				String natDev = rs.getString("nativeDevice");
				String comDev = rs.getString("commonDevice");
				try {
					JSONObject nativeDevice = new JSONObject(natDev);
					JSONArray commonDevice = new JSONArray(comDev);
					addLookup(nativeDevice, commonDevice, lookupTable);
				} catch (JSONException je) {
					log.error("Error loading device mapping\n" + 
					"\tnativeDevice : " + natDev+ "\n" + 
					"\tcommonDevice : " + comDev+ "\n"
					, je);
				}
			}
		} catch (SQLException e) {
			log.error("Error occured while loading device mappings from datbase", e);
			e.printStackTrace();
		}

		db.closeNoThrow(conn);
	}
	public static String addLookup(JSONObject nativeDevice, JSONArray commonDevice,Hashtable<JSONObject, JSONArray> lookupTable) {
//		log.info("\tAdding lookup " + nativeDevice.toString() + " <--> " + commonDevice.toString());
		lookupTable.put(nativeDevice, commonDevice);
		return "successful";
	}

	public static void addDeviceMappingToDatbase(Database db, String deviceName, String interfaceType, String image, String commonDevice, String nativeDevice) {
		Connection conn = db.getHomeAutomationDBConnection();
		
		System.out.println("deviceName : " + deviceName);
		System.out.println("interfaceType : " + interfaceType);
		System.out.println("image : " + image);
		System.out.println("commonDevice : " + commonDevice);
		System.out.println("nativeDevice : " + nativeDevice);
		
		String insertQuery = "INSERT INTO `deviceMap` (`interfaceType`, `deviceName`, `image`, `commonDevice`, `nativeDevice`) " + 
				"VALUES (?,?,?,?,?);";
		
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(insertQuery);
			stmt.setString(1, interfaceType);
			stmt.setString(2, deviceName);
			stmt.setString(3, image);
			stmt.setString(4, commonDevice);
			stmt.setString(5, nativeDevice);
			stmt.executeUpdate();
		} catch (SQLException e) {
			log.error("Error while adding device to device map",e);
		}

	}
}
