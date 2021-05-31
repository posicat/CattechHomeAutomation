package org.cattech.homeAutomation.watchCat;

import org.json.JSONObject;

public class WatchCatEvent {

	private String title;
	private String identifier;
	private JSONObject reaction;

	public WatchCatEvent(String title, String identifier, JSONObject reaction) {
		this.title = title;
		this.identifier = identifier;
		this.reaction = reaction;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public JSONObject getReaction() {
		return this.reaction;
	}

	public void setReaction(JSONObject reaction) {
		this.reaction = reaction;
	}

}
