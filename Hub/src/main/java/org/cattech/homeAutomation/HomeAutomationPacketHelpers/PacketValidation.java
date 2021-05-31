package org.cattech.homeAutomation.HomeAutomationPacketHelpers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cattech.homeAutomation.moduleBase.HomeAutomationPacket;

public class PacketValidation {
	private static Logger log = LogManager.getLogger(PacketValidation.class.getName());

	public static boolean validatePackageMatchesExpectedFormat(HomeAutomationPacket hap) {
		boolean check = true;

		boolean registerStatusOrDestination = false;
		
		if (hap.hasWrapper("register")) { registerStatusOrDestination = true; }
		if (hap.hasWrapper("status")) { registerStatusOrDestination = true;	}
		if (hap.hasWrapper("destination")) { 
			registerStatusOrDestination = true;
			
			if (! hap.hasData()) {
					check=false; log.error("Packet with destination should contain Data");
			}else {
				boolean nativeOrCommonDevice=true;
				
				if (hap.hasData("nativeDevice")) { nativeOrCommonDevice=true;}
				if (hap.hasData("device")) { nativeOrCommonDevice=true;}

				if (! nativeOrCommonDevice) {check=false; log.error("Data block should have a native or common device");}

			}
		}
		
		if (! registerStatusOrDestination) {check=false; log.error("Packet should be of one of the recognized types.");}
		
		
		
		
		return check;
	}
	

}
