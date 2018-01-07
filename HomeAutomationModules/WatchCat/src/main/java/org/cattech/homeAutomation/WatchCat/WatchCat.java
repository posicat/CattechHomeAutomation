package org.cattech.homeAutomation.WatchCat;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;
import org.cattech.homeAutomation.communicationHub.ChannelController;
import org.cattech.homeAutomation.moduleBase.HomeAutomationModule;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;
import org.cattech.homeAutomation.watchCat.WatchCatDatabaseHelper;
import org.cattech.homeAutomation.watchCat.WatchCatEvent;
import org.json.JSONObject;

public class WatchCat extends HomeAutomationModule {
	private static final int WAIT_BETWEEN_WATCHCAT_POLING = 1000 * 60;
	static Logger log = Logger.getLogger(WatchCat.class.getName());

	public WatchCat(ChannelController controller) {
		super(controller);
	}

	@Override
	public void run() {
		this.running = true;

		while (this.running) {
			sleepNoThrowWhileWatchingForIncomingPackets(WAIT_BETWEEN_WATCHCAT_POLING);

			// log.debug("Scanning for overdue events ...");
			Connection conn = getHomeAutomationDBConnection();
			List<WatchCatEvent> events = WatchCatDatabaseHelper.getOverdueEvents(conn);

			for (WatchCatEvent event : events) {
				log.debug("Overdue Event :" + event.getTitle() + " [" + event.getIdentifier() + "]");
				List<JSONObject> actions = getActionsForReaction(conn, event.getReaction());
				for (JSONObject action : actions) {
					if (action.has("destination")) {
						action.put("source", "WatchCat");
						String identifier = event.getIdentifier();
						log.error("Action away ..." + action);
						hubInterface.sendDataToController(action.toString());
						WatchCatDatabaseHelper.updateNextTrigger(conn, identifier);
					} else {
						log.error("Action has no destination." + action);
					}
				}
			}
			closeNoThrow(conn);
		}
	}


	@Override
	public String getModuleChannelName() {
		return "WatchCat";
	}

	@Override
	protected void processPacketRequest(HomeAutomationPacket incoming, List<HomeAutomationPacket> outgoing) {
		// TODO Auto-generated method stub

	}

}
