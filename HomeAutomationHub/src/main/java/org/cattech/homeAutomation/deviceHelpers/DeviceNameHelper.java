package org.cattech.homeAutomation.deviceHelpers;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class DeviceNameHelper {
	static Logger log = Logger.getLogger(DeviceNameHelper.class.getName());

	public static boolean commonDescriptorsMatch(JSONArray fullDescriptor, JSONArray possibleSubDescriptor) {
		int keysMatched = 0;

		for (Object entry : possibleSubDescriptor) {
			List<Object> mightEntries = fullDescriptor.toList();
			if (mightEntries.contains(entry)) {
				keysMatched++;
			}
		}
		// log.debug("Matching " + mightMatch + " <to> " + toMatch +
		// "["+keysMatched+"/"+toMatch.length()+"]");
		return (keysMatched == possibleSubDescriptor.length());
	}

	public static boolean nativeKeysMatch(JSONObject mightMatch, JSONObject toMatch) {
		int keysMismatched = 0;
		for (String key : toMatch.keySet()) {
			String mm = null;
			String tm = null;
			if (!key.equals("controlChannel")) {
				if (mightMatch.has(key)) {
					mm = mightMatch.getString(key).toUpperCase();
					if (toMatch.has(key)) {
						tm = toMatch.getString(key).toUpperCase();

						if (!mightMatch.has(key) || !mm.equals(tm)) {
							keysMismatched++;
//							 log.debug("Didn't match " + key + " : " + mm + "::" + tm);
						} else {
//							 log.debug("Matched " + key + " : " + mm + "::" + tm);
						}
					}
				}
			}
		}
		boolean matched = keysMismatched == 0;
		if (matched) {
//			 log.debug("Match " + mightMatch + " <to> " + toMatch + "[" + keysMismatched +
//			 "/" + toMatch.length() + "]");
		} else {
//			 log.debug("Differ " + mightMatch + " <to> " + toMatch + "[" + keysMismatched
//			 + "/" + toMatch.length() + "]");
		}
		return (matched);
	}

}
