package org.cattech.homeAutomation.moduleBaseTest;

import org.cattech.homeAutomation.communicationHubTest.BaseTestWithController;

public class BaseTestForModules extends BaseTestWithController {

	protected void addTranslationToResolver(String nativeDevice, String commonDevice) {
		String message = 
				"{\"destination\":[\"DeviceResolver\"],"+testPacketSource+",\"data\":{"+
						"\"resolution\":\"addLookup\","+
						"\"nativeDevice\":"+nativeDevice+","+
						"\"commonDevice\":"+commonDevice+
						"}}";
		
		testInterface.sendDataToController(message);
		
		String result = waitforResult(testInterface,1000*10);
		System.out.println(result);
	}

}
