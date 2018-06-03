package datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.ws.rs.core.NoContentException;

import com.fasterxml.jackson.databind.JsonNode;

import clients.SymBotClient;
import exceptions.SymClientException;
import model.UserInfo;

public class User {
	
	static List<User> users = new ArrayList<User>();

	public User(JsonNode usersNodes) {
		name = usersNodes.get("Name").asText();
		email = usersNodes.get("Email").asText();
		profiles = new ArrayList<Profile>();
		notifications = usersNodes.get("Notifications").asBoolean();

		Consumer<JsonNode> profileConsumer = (JsonNode profileNode) -> {
			try {
				addProfile(profileNode.asText());
			} catch (Exception e) {
				e.getMessage();
				e.printStackTrace();
			}
		};
		usersNodes.get("Profiles").forEach(profileConsumer);
	}
	
	public static void updateUserInfo ( SymBotClient botClient) throws NoContentException, SymClientException
	{
		for (User user : users)
		{
			user.setUserInfo(botClient.getUsersClient().getUserFromEmail(user.email, true));
		}
	}

	void addProfile(String profileName) throws Exception {
		Profile p = Profile.getProfile(profileName);
		if (p == null) {
			System.out.println("Unknown profile " + profileName + " for " + name);
		} else {
			profiles.add(p);
		}
	}
	
	public static List<UserInfo> getImpactedUsers (JsonNode incident)
	{
		List<UserInfo> impactedUsers = new ArrayList<UserInfo>();
		
		for (User user : users)
		{
			if (user.notify(incident))
			{
				impactedUsers.add(user.userInfo);
			}
		}
		return impactedUsers;
	}
	
	boolean notify ( JsonNode incident )
	{
		for (Profile profile : profiles)
		{
			if ( profile.filter.match(incident) )
				return true;
		}
		return false;
	}
	
	public static void addUser(User user)
	{
		users.add(user);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Profile> getProfiles() {
		return profiles;
	}

	public void setProfiles(List<Profile> profiles) {
		this.profiles = profiles;
	}

	public boolean isNotifications() {
		return notifications;
	}

	public void setNotifications(boolean notifications) {
		this.notifications = notifications;
	}

	String name;
	String email;
	List<Profile> profiles;
	boolean notifications;
	UserInfo userInfo; //SymphonyUserInfo

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

}
