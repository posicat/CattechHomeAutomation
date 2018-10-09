package org.cattech.homeAutomation.ProcessManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class RestartableProcess {
	private Logger log = Logger.getLogger(this.getClass());
	private int maxRestartsInWindow = 3;
	private int windowMillis = 1000 * 60 * 5;

	private Process process;
	private long lastStart;
	private int starts;
	private String command;
	private boolean enabled = true;
	private String logFile;

	public RestartableProcess(String command, String logFile) throws IOException, RestartableProcessException {
		this.command = command;
		this.lastStart = 0;

		this.logFile = logFile;
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

		if (starts > maxRestartsInWindow) {
			throw new RestartableProcessException("Process restarted too many times within window.");
		}

		this.lastStart = System.currentTimeMillis();
		this.process = Runtime.getRuntime().exec(command);
	}

	public void handleOutputLogging() {
		try {
			String stderr = getStderr();
			if (null!=stderr && !stderr.isEmpty()) {
				log.error("STDERR "+command+" : "+stderr);
				Files.write(Paths.get(logFile), stderr.getBytes(), StandardOpenOption.CREATE,
						StandardOpenOption.APPEND);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("IOException",e);
		}
		try {
			String stdout = getStdout();
			if (null!=stdout && !stdout.isEmpty())
			log.error("STDOUT "+command+" : "+stdout);
			Files.write(Paths.get(logFile), stdout.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("IOException",e);
		}
	}

	public String getCommand() {
		return this.command;
	}

	public String getStdout() {
		String output = "";
		try {
			output = IOUtils.toString(process.getInputStream(), "UTF-8");
		} catch (IOException e) {
			log.error("IOException",e);
		}
		return output;
	}

	public String getStderr() {
		String output = "";
		try {
			output = IOUtils.toString(process.getErrorStream(), "UTF-8");
		} catch (IOException e) {
			log.error("IOException",e);
		}
		return output;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
