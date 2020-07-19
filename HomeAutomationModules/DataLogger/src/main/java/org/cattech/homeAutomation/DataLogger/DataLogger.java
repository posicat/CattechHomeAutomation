package org.cattech.homeAutomation.DataLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.deviceHelpers.DeviceNameHelper;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataLogger extends HomeAutomationModule {
	static Logger log = Logger.getLogger(DataLogger.class.getName());
	private ArrayList<LogLookup> logLookup;

	public DataLogger(ChannelController controller) {
		super(controller);

		loadLoggingSettings();
		verifyTablesSetup();

		// We need to listen to all events to find out what to log, so listen to the
		// EventHandler
		HomeAutomationPacket hap = new HomeAutomationPacket("{\"register\":[\"" + HomeAutomationPacket.CHANNEL_EVENT_HANDLER + "\"],\"nodeName\":\"" + getModuleChannelName() + "\"}");
		hubInterface.sendDataPacketToController(hap);
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		if (incoming.hasWrapper()) {
			if (!incoming.hasData()) {
				log.error("Packet has no data element. " + incoming);
			} else {
				if (incoming.hasData(HomeAutomationPacket.FIELD_DATA_DEVICE)) {
					JSONArray deviceArray = incoming.getData().getJSONArray(HomeAutomationPacket.FIELD_DATA_DEVICE);

					log.debug("Checking packet for logging : " + incoming);

					for (Iterator<LogLookup> iterator = logLookup.iterator(); iterator.hasNext();) {
						LogLookup ll = iterator.next();
						if (DeviceNameHelper.commonDescriptorsMatch(ll.commonDevice, deviceArray)) {
							logDataToDatabase(ll, incoming.getData());
						}
					}
				}
			}
		}
	}

	private void logDataToDatabase(LogLookup ll, JSONObject data) {
		Connection conn = getHomeAutomationDBConnection();
		if (null == conn) {
			log.error("Could not obtain connection to HomeAutomation database");
			return;
		}
		String query = "";
		PreparedStatement stmt = null;
		try {
			log.info("Logging   : " + ll.deviceName);

			query = "INSERT into "+configuration.getDataLoggingDB()+"." + ll.dataTable + " (deviceMap_id,eventTime,value,data) VALUES(?,?,?,?)";

			String eventTime = findStringInJsonByPath(data, ll.eventTime);
			String val = findStringInJsonByPath(data, ll.numericValue);
			String extra = findStringInJsonByPath(data, ll.additionalValue);

			if (null != val || null != extra) {
				stmt = conn.prepareStatement(query);
				stmt.setInt(1, Integer.valueOf(ll.deviceMapID).intValue());
				if (null == eventTime) {
					Timestamp now = new Timestamp(Calendar.getInstance().getTime().getTime());
					stmt.setTimestamp(2, now);
				} else {
					stmt.setTimestamp(2, Timestamp.valueOf(eventTime));
				}
				stmt.setFloat(3, Float.valueOf(val).floatValue());
				stmt.setString(4, extra);
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			log.error("Error logging data to database" + query, e);
			log.error("Table     : " + ll.dataTable);
			log.error("Time      : " + ll.eventTime);
			log.error("Value     : " + ll.numericValue);
			log.error("Add Value : " + ll.additionalValue);
		}

		closeNoThrow(conn);
	}

	private String findStringInJsonByPath(JSONObject data, String path) {
		String found = null;
		if (null != path) {
			String[] pathArr = path.split("/", 2);

			try {

				if (pathArr.length > 1) {
					if (data.has(pathArr[0])) {
						found = findStringInJsonByPath(data.getJSONObject(pathArr[0]), pathArr[1]);
					}
				} else {
					if (data.has(path)) {
						found = data.get(path).toString();
					}
				}
			} catch (JSONException e) {
				// We won't match all JSON structures, if we don't, we simply return our default
				// value here.
			}
		}
		return found;
	}

	@Override
	public String getModuleChannelName() {
		return "DataLogger";
	}

	public void loadLoggingSettings() {
		Connection conn = getHomeAutomationDBConnection();
		if (null == conn) {
			log.error("Could not obtain connection to HomeAutomation database");
			return;
		}

		this.logLookup = new ArrayList<LogLookup>();

		Statement stmt;
		ResultSet rs;
		try {
			stmt = conn.createStatement();
			String query = "SELECT dl.deviceMap_id,dataTable,eventTime,numericValue,additionalValue,commonDevice,deviceName FROM dataLogging dl " + " LEFT JOIN  deviceMap dm on dm.deviceMap_id = dl.deviceMap_id ";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				if (null != rs.getString("commonDevice")) {
					LogLookup ll = new LogLookup();
					ll.commonDevice = new JSONArray(rs.getString("commonDevice"));
					ll.deviceName = rs.getString("deviceName");
					ll.dataTable = rs.getString("dataTable");
					ll.eventTime = rs.getString("eventTime");
					ll.numericValue = rs.getString("numericValue");
					ll.additionalValue = rs.getString("additionalValue");
					ll.deviceMapID = rs.getString("dl.deviceMap_id");
					logLookup.add(ll);
				}
			}
		} catch (SQLException e) {
			log.error("Error occured while loading logging settings from datbase", e);
			e.printStackTrace();
		}

		closeNoThrow(conn);
	}

	private void verifyTablesSetup() {
		Connection conn = getHomeAutomationDBConnection();
		if (null == conn) {
			log.error("Could not obtain connection to HomeAutomation database");
			return;
		}

		for (Iterator<LogLookup> iterator = logLookup.iterator(); iterator.hasNext();) {
			LogLookup ll = iterator.next();

			String dataLoggingDB = configuration.getDataLoggingDB();
			try {
				Statement stmt = conn.createStatement();
				stmt.executeQuery("SELECT 1 FROM "+dataLoggingDB+"." + ll.dataTable + " LIMIT 1");
			} catch (SQLException e) {
				// Exception happened, Table does not exist.
				log.info("Creating table : "+dataLoggingDB+"." + ll.dataTable);

				String query = "CREATE TABLE "+dataLoggingDB+"." + ll.dataTable + "( " + " "
						+ ll.dataTable + "_id INT NOT NULL AUTO_INCREMENT, "
						+ " deviceMap_id INT NOT NULL, "
						+ " eventTime DATETIME DEFAULT CURRENT_TIMESTAMP, "
						+ " value FLOAT, " + " data text"
						+ ", PRIMARY KEY(" + ll.dataTable + "_id)) ENGINE=InnoDB DEFAULT CHARSET=latin1;";

				try {
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(query);
				} catch (SQLException e2) {
					log.error("Could not create new table " + ll.dataTable + "\n" + query, e2);
				}
			}
		}

		closeNoThrow(conn);
	}

}
