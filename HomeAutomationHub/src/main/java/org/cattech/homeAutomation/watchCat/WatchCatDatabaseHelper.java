package org.cattech.homeAutomation.watchCat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class WatchCatDatabaseHelper {
	static Logger log = Logger.getLogger(WatchCatDatabaseHelper.class.getName());

	public static String generateEventSignature(String server, JSONObject data) {
		return jsonElementByType(null, data) + "@" + server;
	}

	private static String jsonObjectToSignature(JSONObject data) {
		// log.debug("jsonObjectToSignature(" + data.toString() + ")");
		String[] names = JSONObject.getNames(data);
		Arrays.sort(names);
		String result = "";
		for (int i = 0; i < names.length; i++) {
			result += jsonElementByType(names[i], data.get(names[i]));
			if (i < names.length - 1) {
				result += ",";
			}
		}
		return result;
	}

	private static String jsonArrayToSignature(JSONArray elem) {
		// log.debug("jsonArrayToSignature(" + elem.toString() + ")");
		String result = "";
		for (int j = 0; j < elem.length(); j++) {
			result += jsonElementByType(null, elem.get(j));
			if (j < elem.length() - 1) {
				result += ",";
			}
		}
		return result;
	}

	private static String jsonElementByType(String name, Object elem) {
		// log.debug("jsonElementByType(" + name + "," + elem.toString() + ")");
		String result = "";
		if (name != null) {
			result = name + ":";
		}

		if (elem instanceof JSONObject) {
			result += "{" + jsonObjectToSignature((JSONObject) elem) + "}";
		} else if (elem instanceof JSONArray) {
			result += "[" + jsonArrayToSignature((JSONArray) elem) + "]";
		} else {
			result += elem.toString();
		}
		return result;
	}

	public static Boolean afterMinDelay(Connection conn, String identifier) {
		PreparedStatement stmt;
		ResultSet rs;

		String query = "SELECT minFrequency,TIMESTAMPDIFF(SECOND,now(),nextMin) as delta FROM watchCat WHERE identifier=?";
		Boolean after = null;

		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, identifier);
			rs = stmt.executeQuery();
			if (rs.next()) {
				String mf = rs.getString("minFrequency");
				int delta = rs.getInt("delta");
				if (null != mf) {
					if (delta > 0) {
						after = true;
					} else {
						after = false;
					}
				}
			}
		} catch (SQLException e) {
			log.error("Problem determining afterMinDelay", e);
		}
		return after;
	}

	public static Boolean afterMaxDelay(Connection conn, String identifier) {
		PreparedStatement stmt;
		ResultSet rs;

		String query = "SELECT maxFrequency,TIMESTAMPDIFF(SECOND,now(),nextMax) as delta FROM watchCat WHERE identifier=?";
		Boolean after = null;

		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, identifier);
			rs = stmt.executeQuery();
			if (rs.next()) {
				String mf = rs.getString("minFrequency");
				int delta = rs.getInt("delta");
				if (null != mf) {
					if (delta > 0) {
						after = true;
					} else {
						after = false;
					}
				}
			}
		} catch (SQLException e) {
			log.error("Problem determining afterMinDelay", e);
		}
		return after;
	}

	public static void updateLastEvent(Connection conn, String identifier) {
		PreparedStatement stmt;
		ResultSet rs;

		String query = "SELECT minFrequency,maxFrequency FROM watchCat WHERE identifier=?";

		String minF = null;
		String maxF = null;

		boolean update = false;
		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, identifier);
			rs = stmt.executeQuery();
			if (rs.next()) {
				minF = rs.getString("minFrequency");
				maxF = rs.getString("maxFrequency");
				update = true;
			}
		} catch (SQLException e) {
			log.error("Problem determining frequencies", e);
		}

		if (update) {
			query = "UPDATE watchCat SET lastEvent=now()";
			if (null != minF) {
				query += ",nextMin=DATE_ADD(now(),INTERVAL " + minF + ")";
			}
			if (null != maxF) {
				query += ",nextMax=DATE_ADD(now(),INTERVAL " + maxF + ")";
			}
			query += " WHERE identifier=?";
		} else {
			query = "INSERT INTO watchCat SET lastEvent=now(),identifier=?";
		}

		log.info("Update Query :" + query);

		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, identifier);
			stmt.executeUpdate();
		} catch (SQLException e) {
			log.error("Problem updating 'next' fields.", e);
		}

	}

}
