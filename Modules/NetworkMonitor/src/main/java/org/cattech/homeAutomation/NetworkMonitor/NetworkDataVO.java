package org.cattech.homeAutomation.NetworkMonitor;

public class NetworkDataVO {
	private static final byte ARP_FLAG_COMPLETE = 0x2;
	
	public int networkNames_id;
	public byte status;
	public String mac;
	public String ip;
	public String lastSeen;
	public String netDevice;
	public String deviceName;
	public String monitorLevel;
	boolean dirty = false;

	public String name;



	public NetworkDataVO(String arpData) {
		String[] ad = arpData.split("\\s+");
		
		ip = ad[0];
//		hwType = ad[1];
		status = Byte.valueOf(ad[2]).byteValue();
		mac = ad[3];
//		mask = ad[4];
		netDevice = ad[5];
	}
	
	public NetworkDataVO() {
		this.dirty=true;
	}
	
	public void registerCurrentState(NetworkDataVO nd) {

		if ((nd.status & ARP_FLAG_COMPLETE) > 0)   {
			java.util.Date today = new java.util.Date();
			this.lastSeen = new java.sql.Time(today.getTime()).toString();
			this.dirty=true;
		}

		//MAC is the key, will remain the same
		ip=nd.ip;
		status = nd.status;
		netDevice = nd.netDevice;
	}

}
