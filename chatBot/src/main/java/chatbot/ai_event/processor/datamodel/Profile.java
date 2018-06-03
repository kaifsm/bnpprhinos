package chatbot.ai_event.processor.datamodel;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class Profile {

	static Map<String, Profile> profiles = new HashMap<String, Profile>();

	String name;
	Filter filter;

	public Profile(JsonNode profileNode) {
		if (profileNode.has("Profile name"))
			name = profileNode.get("Profile name").asText();
		else
			name = "undefined";
		if (profileNode.has("Notification filter"))
			filter = new Filter(profileNode.get("Notification filter"));
		else
			filter = null;
	}

	public static void addProfile(Profile profile) {
		profiles.put(profile.name, profile);
	}

	static Profile getProfile(String profileName) {
		return profiles.get(profileName);
	}
}
