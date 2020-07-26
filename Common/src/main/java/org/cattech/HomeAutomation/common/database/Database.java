package org.cattech.HomeAutomation.common.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.common.configuration.HomeAutomationConfiguration;


public class Database {
	static Logger log = Logger.getLogger(Database.class.getName());

	protected HomeAutomationConfiguration configuration;
	
	public Database(HomeAutomationConfiguration configuration) {
		this.configuration = configuration;
	}

	public Connection getHomeAutomationDBConnection() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(configuration.getDBURL());
		} catch (SQLException ex) {
			log.error("Error getting sql connection to :" + configuration.getDBURL(), ex);
			log.error("SQLState: " + ex.getSQLState());
			log.error("VendorError: " + ex.getErrorCode());
		}
		return conn;
	}

	public void closeNoThrow(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {
			log.error("Error closeing DB connection", e);
		}
	}
}
