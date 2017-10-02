package org.cattech.homeAutomation.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class homeAutomationConfiguration {

	private static final String ENV_FOLDER_CONFIG = "HOMEAUTOMATION_CONFIG";
	private static final String ENV_FOLDER_MODULES = "HOMEAUTOMATION_MODULES";
	private static final String ENV_FOLDER_LIBRARIES = "HOMEAUTOMATION_LIB";

	private static Properties props = new Properties();
	private Logger log = Logger.getLogger(this.getClass());

	public homeAutomationConfiguration() {

	}

	public void initialize() throws HomeAutomationConfigurationException {
		try {
			props.load(getClass().getResourceAsStream("/org/cattech/homeAutomation/HomeAutomation.properties"));
		} catch (IOException|NullPointerException e) {
			log.warn("Could not load jar-internal configuration",e);
		}
		
		String home = overridePropsWithEnvironment("homeAutomation.home",ENV_FOLDER_CONFIG);
		throwIfNotExistantDirectory(home);

		String modules = overridePropsWithEnvironment("homeAutomation.modules",ENV_FOLDER_MODULES);
		throwIfNotExistantDirectory(modules);

		String libraries = overridePropsWithEnvironment("homeAutomation.lib",ENV_FOLDER_LIBRARIES);
		throwIfNotExistantDirectory(libraries);
	}

	// ====================================================================================================
	// Helper Methods
	// ====================================================================================================

	private static String overridePropsWithEnvironment(String propName, String envName) {
		if (null != System.getenv(envName)) {
			props.setProperty(propName, System.getenv(envName));
		}
		return props.getProperty(propName);
	}

	public void loadConfiguration() throws HomeAutomationConfigurationException {
		String configFolder = getConfigFolder().replace("\\", "/");
		FileInputStream input;
		try {
			input = new FileInputStream(configFolder + "/settings.conf");
			props.load(input);
		} catch (IOException e) {
			throw new HomeAutomationConfigurationException("Could not find configuration file, please set " + ENV_FOLDER_CONFIG, e);
		}
	}
	// ====================================================================================================

	private static void throwIfNotExistantDirectory(String dir) throws HomeAutomationConfigurationException {
		if (null==dir) {
			throw (new HomeAutomationConfigurationException("Null folder name"));
		}
		
		File f = new File(dir);

		if (!f.exists()) {
			throw (new HomeAutomationConfigurationException("Folder " + dir + " does not exist"));
		}
		if (!f.isDirectory()) {
			throw (new HomeAutomationConfigurationException("Path " + dir + " is not a folder"));
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

	// ================================================================================

}
