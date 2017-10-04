package org.cattech.homeAutomation.ProcessManager;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class RestartableProcess {
	private int		maxRestartsInWindow	= 3;
	private int		windowMillis		= 1000 * 60 * 5;

	private Process	process;
	private long	lastStart;
	private int		starts;
	private String	command;
	private boolean	enabled				= true;

	public RestartableProcess(String command) throws IOException, RestartableProcessException {
		this.command = command;
		this.lastStart = 0;
		this.starts = 0;
		restartProcess();
	}

	public boolean isAlive() {
		return process.isAlive();
	}

	public void restartProcess() throws IOException, RestartableProcessException {
		long dTime = System.currentTimeMillis() - lastStart;
		starts++;
		if (dTime > windowMillis) {
			starts = 0;
		}

		if (starts > maxRestartsInWindow) { throw new RestartableProcessException(
				"Process restarted too many times within window."); }

		this.lastStart = System.currentTimeMillis();
		this.process = Runtime.getRuntime().exec(command);
	}

	public String getCommand() {
		return this.command;
	}

	public String getStdout() {
		String output = "";
		try {
			output = IOUtils.toString(process.getInputStream(), "UTF-8");
		} catch (IOException e) {}
		return output;
	}

	public String getStderr() {
		String output = "";
		try {
			output = IOUtils.toString(process.getErrorStream(), "UTF-8");
		} catch (IOException e) {}
		return output;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
