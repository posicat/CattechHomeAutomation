package org.cattech.HomeAutomation.servlets;

import java.sql.Connection;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cattech.HomeAutomation.common.database.Database;
import org.cattech.homeAutomation.common.configuration.HomeAutomationConfiguration;
import org.cattech.homeAutomation.common.configuration.HomeAutomationConfigurationException;

public class HomeAutoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String group;
	private Cookie[] cookies; 
	
	static Logger log = Logger.getLogger(HomeAutoServlet.class.getName());

	
	public HomeAutoServlet() {
		
	}
	
	public void loadCurrentState(HttpServletRequest request, HttpServletResponse response) {
		group = request.getParameter("group");
		
		cookies = request.getCookies();
		
		HomeAutomationConfiguration configuration = null;
		try {
			configuration = new HomeAutomationConfiguration(true);
		} catch (HomeAutomationConfigurationException e) {
			log.error("Could not load configuration",e);
		}
		Database db = new Database(configuration);
		
		Connection conn = db.getHomeAutomationDBConnection();
		
	}
}
