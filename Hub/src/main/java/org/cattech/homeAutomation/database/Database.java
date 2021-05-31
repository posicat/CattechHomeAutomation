package org.cattech.homeAutomation.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.configuration.HomeAutomationConfiguration;


public class Database {
	static Logger log = LogManager.getLogger(Database.class.getName());

	protected HomeAutomationConfiguration configuration;
	
	public Database(HomeAutomationConfiguration configuration) {
		this.configuration = configuration;
	}

	public Connection getHomeAutomationDBConnection() {
		Connection conn = null;
		String dburl = configuration.getDBURL();
			try {
				conn = DriverManager.getConnection(dburl);
			} catch (SQLException ex) {
				log.error("Error getting sql connection to :" + dburl, ex);
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
