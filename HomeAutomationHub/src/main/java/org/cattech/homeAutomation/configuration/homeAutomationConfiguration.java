package org.cattech.homeAutomation.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class homeAutomationConfiguration {

	private static final String ENV_FOLDER_CONFIG = "HOMEAUTOMATION_CONFIG";
	private static final String ENV_FOLDER_MODULES = "HOMEAUTOMATION_MODULES";
	private static final String ENV_FOLDER_LIBRARIES = "HOMEAUTOMATION_LIB";

	private static Properties props = new Properties();

	public homeAutomationConfiguration() {

	}

	public void initialize() throws HomeAutomationConfigurationException {
		String os = System.getProperty("os.name");

		// Home Directory Config
		String home = System.getenv(ENV_FOLDER_CONFIG);
		if (null == home) {
			if (os.matches(".*Windows.*")) {
				home = "C:/homeAutomation/";
			}
			if (os.matches(".*Linux.*")) {
				home = "/etc/homeAutomation/";
			}
		}
		throwIfNotExistantDirectory(home);
		props.setProperty("homeAutomation.home", home);

		// Modules Directory Modules
		String modules = System.getenv(ENV_FOLDER_MODULES);
		if (null == modules) {
			if (os.matches(".*Windows.*")) {
				modules = "C:/homeAutomation/modules/";
			}
			if (os.matches(".*Linux.*")) {
				modules = "/usr/local/bin/homeAutomation/modules/";
			}
		}
		throwIfNotExistantDirectory(home);
		props.setProperty("homeAutomation.modules", modules);

		// Libraries Directory Config
		String libraries = System.getenv(ENV_FOLDER_LIBRARIES);
		if (null == libraries) {
			if (os.matches(".*Windows.*")) {
				libraries = "C:/homeAutomation/lib/";
			}
			if (os.matches(".*Linux.*")) {
				libraries = "/usr/local/bin/homeAutomation/lib/";
			}
		}
		throwIfNotExistantDirectory(libraries);
		props.setProperty("homeAutomation.lib", libraries);

	}

	// ====================================================================================================
	// Helper Methods
	// ====================================================================================================

	public void loadConfiguration() throws IOException {
		String configFolder = getConfigFolder().replace("\\", "/");
		FileInputStream input;
		try {
			input = new FileInputStream(configFolder + "/settings.conf");
			props.load(input);
		} catch (IOException e) {
			throw new IOException("Could not find configuration file, please set " + ENV_FOLDER_CONFIG, e);
		}
	}
	// ====================================================================================================

	private void throwIfNotExistantDirectory(String dir) throws HomeAutomationConfigurationException {
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
