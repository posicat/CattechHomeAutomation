package org.cattech.homeAutomation.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class HomeAutomationConfiguration {

	private static final String ENV_FOLDER_CONFIG = "HOMEAUTOMATION_CONFIG";
	private static final String ENV_FOLDER_MODULES = "HOMEAUTOMATION_MODULES";
	private static final String ENV_FOLDER_LIBRARIES = "HOMEAUTOMATION_LIB";
	private static final String ENV_FOLDER_HOME = "HOMEAUTOMATION_HOME";
	private static final String ENV_FOLDER_LOGS = "HOMEAUTOMATION_LOG";

	private static Properties props = new Properties();
	private Logger log = Logger.getLogger(this.getClass());

	public HomeAutomationConfiguration() throws HomeAutomationConfigurationException {
		initialize();
	}

	public HomeAutomationConfiguration(boolean initialize) throws HomeAutomationConfigurationException {
		if (initialize) {
			initialize();
		}
	}

	private void initialize() throws HomeAutomationConfigurationException {
		try {
			props.load(getClass().getClassLoader().getResourceAsStream("homeAutomation.properties"));
		} catch (IOException | NullPointerException e) {
			log.error("Could not load jar-internal configuration", e);
		}

		props.getProperty("mail.smtp.host", "localhost");
		overridePropsWithEnvironment("homeAutomation.config", ENV_FOLDER_HOME);
		overridePropsWithEnvironment("homeAutomation.config", ENV_FOLDER_CONFIG);
		overridePropsWithEnvironment("homeAutomation.modules", ENV_FOLDER_MODULES);
		overridePropsWithEnvironment("homeAutomation.lib", ENV_FOLDER_LIBRARIES);
		overridePropsWithEnvironment("homeAutomation.log", ENV_FOLDER_LOGS);
	
		setLogFileForAppender("console", "org.cattech", "HomeautomationHub.log", null);
		StdOutErrLog.tieSystemOutAndErrToLog();

		loadConfiguration();
	}

	public void setLogFileForAppender(String loggerName, String clazz, String logFile, Level level) {
		Logger rootLogger = Logger.getRootLogger();
		ConsoleAppender consoleAppender = (ConsoleAppender) rootLogger.getAppender("console"); // Matching format to original appender.

		FileAppender fileAppender;
		try {
			fileAppender = new FileAppender(consoleAppender.getLayout(), getLogFolder() + "/" + logFile);
			fileAppender.setName(loggerName);
			fileAppender.activateOptions();
			Logger classLogger = org.apache.log4j.Logger.getLogger(clazz);
			classLogger.setAdditivity(false);
			if (level != null) {
				classLogger.setLevel(level);
			}
			classLogger.addAppender(fileAppender);
		} catch (IOException e) {
			log.error("Failed to add log file", e);
		}

	}

	// ====================================================================================================
	// Helper Methods
	// ====================================================================================================

	public static class StdOutErrLog {

		private static final Logger logger = Logger.getLogger(StdOutErrLog.class);

		public static void tieSystemOutAndErrToLog() {
			System.setOut(createLoggingProxy(System.out));
			System.setErr(createLoggingProxy(System.err));
		}

		public static PrintStream createLoggingProxy(final PrintStream realPrintStream) {
			return new PrintStream(realPrintStream) {
				public void print(final String string) {
					realPrintStream.print(string);
					logger.info(string);
				}
			};
		}
	}

	private static String overridePropsWithEnvironment(String propName, String envName) throws HomeAutomationConfigurationException {
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

	private static void throwIfNotExistantDirectory(String prop, String dir) throws HomeAutomationConfigurationException {
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
		String url = null;
		if (null != props.getProperty("db.host")) {
			url = "jdbc:mysql://" + props.getProperty("db.host") + "/" + props.getProperty("db.name") + "?" + "useSSL=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC" + "&user="
					+ props.getProperty("db.username") + "&password=" + props.getProperty("db.password");
		} else {
			log.info("No host set for SQL, returning null");
		}
		return url;
	}

	public String getHost() {
		return props.getProperty("hub.host", "localhost");
	}

	// ================================================================================

}
