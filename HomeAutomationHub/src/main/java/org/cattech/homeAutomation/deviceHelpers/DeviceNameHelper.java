package org.cattech.homeAutomation.deviceHelpers;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class DeviceNameHelper {

	public static boolean commonDescriptorsMatch(JSONArray mightMatch, JSONArray toMatch) {
		int keysMatched = 0;

		for (Object entry : toMatch) {
			List<Object> mightEntries = mightMatch.toList();
			if (mightEntries.contains(entry)) {
				keysMatched++;
			}
		}
		// log.debug("Matching " + mightMatch + " <to> " + toMatch +
		// "["+keysMatched+"/"+toMatch.length()+"]");
		return (keysMatched == toMatch.length());
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
							// log.debug("Didn't match " + key + " : " + mm + "::" + tm);
						} else {
							// log.debug("Matched " + key + " : " + mm + "::" + tm);
						}
					}
				}
			}
		}
		boolean matched = keysMismatched == 0;
		if (matched) {
			// log.debug("Match " + mightMatch + " <to> " + toMatch + "[" + keysMismatched +
			// "/" + toMatch.length() + "]");
		} else {
			// log.debug("Differ " + mightMatch + " <to> " + toMatch + "[" + keysMismatched
			// + "/" + toMatch.length() + "]");
		}
		return (matched);
	}

}
