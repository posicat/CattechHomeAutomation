package org.cattech.homeAutomation.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

public class homeAutomationConfiguration {

	private static final String ENV_FOLDER_CONFIG = "HOMEAUTOMATION_CONFIG";
	private static final String ENV_FOLDER_MODULES = "HOMEAUTOMATION_MODULES";
	private static final String ENV_FOLDER_LIBRARIES = "HOMEAUTOMATION_LIB";
	private static final String ENV_FOLDER_HOME = "HOMEAUTOMATION_HOME";
	private static final String ENV_FOLDER_LOGS = "HOMEAUTOMATION_LOG";

	private static Properties props = new Properties();
	private Logger log = Logger.getLogger(this.getClass());

	public homeAutomationConfiguration() throws HomeAutomationConfigurationException {
		initialize();
	}

	public homeAutomationConfiguration(boolean initialize) throws HomeAutomationConfigurationException {
		if (initialize) {
			initialize();
		}
	}

	private void initialize() throws HomeAutomationConfigurationException {
		try {
			props.load(getClass().getClassLoader().getResourceAsStream("homeAutomation.properties"));
		} catch (IOException | NullPointerException e) {
			log.warn("Could not load jar-internal configuration", e);
		}

		overridePropsWithEnvironment("homeAutomation.config", ENV_FOLDER_HOME);
		overridePropsWithEnvironment("homeAutomation.config", ENV_FOLDER_CONFIG);
		overridePropsWithEnvironment("homeAutomation.modules", ENV_FOLDER_MODULES);
		overridePropsWithEnvironment("homeAutomation.lib", ENV_FOLDER_LIBRARIES);
		overridePropsWithEnvironment("homeAutomation.log", ENV_FOLDER_LOGS);

		String logFile = getLogFolder() + "/HomeautomationHub.log";
		Logger rootLogger = Logger.getRootLogger();
		ConsoleAppender consoleAppender = (ConsoleAppender) rootLogger.getAppender("console");
		
		FileAppender fileAppender;
		try {
			fileAppender = new FileAppender(consoleAppender.getLayout(), logFile);
			fileAppender.setName("file");
			rootLogger.addAppender(fileAppender);
		} catch (IOException e) {
			log.error("Couldn't set logfile.",e);
		}

		loadConfiguration();
	}

	// ====================================================================================================
	// Helper Methods
	// ====================================================================================================

	private static String overridePropsWithEnvironment(String propName, String envName)
			throws HomeAutomationConfigurationException {
		if (null != System.getenv(envName)) {
			props.setProperty(propName, System.getenv(envName));
		}
		throwIfNotExistantDirectory(propName, props.getProperty(propName));
		return props.getProperty(propName);
	}

	public void loadConfiguration() throws HomeAutomationConfigurationException {
		String configFolder = this.getConfigFolder().replace("\\", "/");
		try {
			String settings = configFolder + "/settings.conf";
			log.info("Loading config from " + settings);
			props.load(new FileInputStream(settings));
		} catch (IOException e) {
			log.error("Could not find configuration file, please set " + ENV_FOLDER_CONFIG, e);
		}
	}
	// ====================================================================================================

	private static void throwIfNotExistantDirectory(String prop, String dir)
			throws HomeAutomationConfigurationException {
		if (null == dir) {
			throw (new HomeAutomationConfigurationException(prop + " : Null folder name"));
		}

		File f = new File(dir);

		if (!f.exists()) {
			throw (new HomeAutomationConfigurationException(prop + " : Folder " + dir + " does not exist"));
		}
		if (!f.isDirectory()) {
			throw (new HomeAutomationConfigurationException(prop + " : Path " + dir + " is not a folder"));
		}

	}
	// ====================================================================================================
	// Getters, setters, etc.
	// ====================================================================================================

	public Properties getProps() {
		return props;
	}

	public String getConfigFolder() {
		return props.getProperty("homeAutomation.config");
	}

	public String getModulesFolder() {
		return props.getProperty("homeAutomation.modules");
	}

	public String getLibFolder() {
		return props.getProperty("homeAutomation.lib");
	}

	public String getLogFolder() {
		return props.getProperty("homeAutomation.log");
	}

	public int getPort() {
		return Integer.parseInt(props.getProperty("hub.port", "10042"));
	}

	public String getBaseURL() {
		return props.getProperty("baseUrl");
	}

	public String getDBURL() {
		String url = "jdbc:mysql://" + props.getProperty("db.host") + "/" + props.getProperty("db.name") + "?"
				+ "useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
				+ "&user=" + props.getProperty("db.username") + "&password=" + props.getProperty("db.password");
		// log.info(url);
		return url;
	}

	// ================================================================================

}
