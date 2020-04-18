package org.cattech.homeAutomation.watchCat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

		String query = "SELECT minFrequency,TIMESTAMPDIFF(SECOND,nextMin,now()) as delta FROM watchCat WHERE identifier=?";
		Boolean after = null;

		log.debug("Query : " + query + " [" + identifier + "]");

		try {
			stmt = conn.prepareStatement(query);
			stmt.setString(1, identifier);
			rs = stmt.executeQuery();
			if (rs.next()) {
				log.debug("Result : " + dumpResultSet(rs));
				String mf = rs.getString("minFrequency");
				int delta = rs.getInt("delta");
				if (null != mf) {
					if (delta > 0) {
						after = Boolean.TRUE;
					} else {
						after = Boolean.FALSE;
					}
				}
			}
		} catch (SQLException e) {
			log.error("Problem determining afterMinDelay", e);
		}
		return after;
	}

	private static String dumpResultSet(ResultSet rs) {
		String result = "{";

		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();
			if (rsmd.getColumnCount() > 0) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					result = result + "'" + rsmd.getColumnLabel(i) + "':'" + rs.getString(i)+",";
				}
			}
			result+="}";
		} catch (SQLException e) {
			log.error("Failed to display ResultSet", e);
		}
		return result;
	}

	public static Boolean afterMaxDelay(Connection conn, String identifier) {
		PreparedStatement stmt;
		ResultSet rs;

		String query = "SELECT maxFrequency,TIMESTAMPDIFF(SECOND,nextMax,now()) as delta FROM watchCat WHERE identifier=?";
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
						after = Boolean.TRUE;
					} else {
						after = Boolean.FALSE;
					}
				}
			}
		} catch (SQLException e) {
			log.error("Problem determining afterMinDelay", e);
		}
		return after;
	}

	public static void updateNextTrigger(Connection conn, String identifier) {
		updateWatchCatEvent(conn, identifier, false, true, true);
	}

	public static void updateEventOccurance(Connection conn, String identifier) {
		updateWatchCatEvent(conn, identifier, true, true, true);
	}

	private static void updateWatchCatEvent(Connection conn, String identifier, boolean updateEvent, boolean updateMin,
			boolean updateMax) {
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

		String insertQuery = "UPDATE watchCat SET ";
		if (update) {
			String updateQuery = "";
			if (updateEvent) {
				updateQuery += ",lastEvent=now()";
			}
			if (null != minF && updateMin) {
				updateQuery += ",nextMin=DATE_ADD(now(),INTERVAL " + minF + ")";
			}
			if (null != maxF && updateMax) {
				updateQuery += ",nextMax=DATE_ADD(now(),INTERVAL " + maxF + ")";
			}

			updateQuery = updateQuery.replaceAll("^,", "");

			insertQuery += updateQuery + " WHERE identifier=?";
		} else {
			insertQuery = "INSERT INTO watchCat SET lastEvent=now(),identifier=?";
		}

		log.info("Add/Update next times Query :" + insertQuery + "[" + identifier + "]");

		try {
			stmt = conn.prepareStatement(insertQuery);
			stmt.setString(1, identifier);
			stmt.executeUpdate();
		} catch (SQLException e) {
			log.error("Problem updating 'next' fields.", e);
		}

	}

	public static List<WatchCatEvent> getOverdueEvents(Connection conn) {
		ArrayList<WatchCatEvent> events = new ArrayList<WatchCatEvent>();

		String query = "SELECT title,identifier,reaction FROM watchCat WHERE nextMax < now() AND nextMin < now()";
		// log.debug(query);
		try {
			PreparedStatement stmt = conn.prepareStatement(query);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String title = rs.getString("title");
				String identifier = rs.getString("identifier");
				String reaction = rs.getString("reaction");
				if (null != reaction) {
					events.add(new WatchCatEvent(title, identifier, new JSONObject(reaction)));
				}
			}
		} catch (SQLException e) {
			log.error("Problem determining expired events", e);
		}

		return events;
	}

}
