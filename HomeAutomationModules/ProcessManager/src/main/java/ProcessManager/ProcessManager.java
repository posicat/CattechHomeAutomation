package ProcessManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import communicationHub.ChannelController;
import moduleBase.HomeAutomationModule;

public class ProcessManager extends HomeAutomationModule {
	ArrayList<RestartableProcess> subProcesses = new ArrayList<RestartableProcess>();
	
	public ProcessManager(ChannelController controller) {
			super(controller);
			Properties props = controller.getProps();
			
			int count = 0;
			while ( props.containsKey("StartProcess."+count)) {
				String command = props.getProperty("StartProcess."+count);
				count++;

				log.info("Starting process : "+command);
				try {
					subProcesses.add(new RestartableProcess(command));
				} catch (IOException | RestartableProcessException e) {
					log.info("Failed to start"+e.getMessage());
				}
			}
		}

	@Override
	public void run() {
		running=true;
		while (running) {
			
			for (RestartableProcess process : subProcesses) {
					log.info("Process :" + process.getCommand() + "has exited.");

					try {
						process.restartProcess();
					} catch (IOException | RestartableProcessException e) {
						log.info("Process :"+process.getCommand() + " failed to restart");
					}
			}
			
			sleepNoThrow(10000);
		}
	}

}
