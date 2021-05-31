package org.cattech.homeAutomation.configuration;

public class HomeAutomationConfigurationException extends Exception {
	private static final long serialVersionUID = 1L;

	public HomeAutomationConfigurationException(String message) {
		super(message);
	}

	public HomeAutomationConfigurationException(String message, Exception e) {
		super(message, e);
	}
}