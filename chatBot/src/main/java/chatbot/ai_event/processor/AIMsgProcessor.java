package chatbot.ai_event.processor;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;

import chatbot.ai_event.processor.datamodel.Filter;
import chatbot.ai_event.processor.datamodel.Profile;
import chatbot.ai_event.processor.datamodel.RoomUtil;
import chatbot.ai_event.processor.datamodel.RoomWrapper;
import chatbot.ai_event.processor.datamodel.User;
import exceptions.SymClientException;
import model.OutboundMessage;
import model.Room;
import model.RoomInfo;
import model.UserInfo;

public class AIMsgProcessor implements Runnable {

	public static LinkedBlockingQueue<JsonNode> messageQueue = new LinkedBlockingQueue<JsonNode>();

	public static void main(String[] args) {
		AIMsgProcessor app = new AIMsgProcessor();
		try {
			app.loadUserConfig();
			app.processIncidentFromJsonFile();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void processIncidentFromJsonFile() throws IOException, SymClientException {
		URL url = getClass().getResource("IssueSample.json");
		JsonNode incidentNode = JsonLoader.fromURL(url);
		processIncident(incidentNode);
	}

	AIMsgProcessor() {
		try {
			synchronized (User.getUsers()) {
				if (User.getUsers().isEmpty()) {
					loadUserConfig();
					User.updateUserInfo(RoomUtil.getBotClient());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void start() {
		ExecutorService service = Executors.newFixedThreadPool(1);
		service.submit(new AIMsgProcessor());
	}

	void loadUserConfig() throws Exception {
		URL url = getClass().getResource("RhinosUsers&Profiles.json");
		System.out.println("Loading users & profiles from file " + url.getPath());
		JsonNode allNodes = JsonLoader.fromURL(url);

		// load filter criterias
		if (!allNodes.has("Incident attributes")) {
			System.out.println("Mssing Incident attributes !!!");
			throw new Exception("Missing Incident attributes !!!");
		}
		Consumer<JsonNode> criteriaConsumer = (JsonNode criteriaNode) -> Filter.addIncidentAttribute(criteriaNode.asText());
		allNodes.get("Incident attributes").forEach(criteriaConsumer);

		// load Profiles
		Consumer<JsonNode> profileConsumer = (JsonNode profileNode) -> Profile.addProfile(new Profile(profileNode));
		allNodes.get("Profiles").forEach(profileConsumer);

		// load Users
		JsonNode usersNode = allNodes.get("Users");
		Consumer<JsonNode> userConsumer = (JsonNode userNode) -> User.addUser(new User(userNode));
		usersNode.forEach(userConsumer);

		// load Room merging Criterias
		Consumer<JsonNode> roomMergingCriteriaConsumer = (JsonNode criteriaNode) -> RoomUtil
				.addMergingCriteria(criteriaNode.asText());
		allNodes.get("Room merging criterias").forEach(roomMergingCriteriaConsumer);

		System.out.println("Loading user&profile successful !!");
	}

	void processIncident(JsonNode incidentNode) throws SymClientException {

		System.out.println("Process issue " + incidentNode.get("issue type"));
		String roomKey = RoomUtil.getRoomKey(incidentNode);
		String roomName = "Alert " + roomKey;

		// Check if room already exist
		if (RoomUtil.getRooms().containsKey(roomKey)) {
			RoomInfo roomInfo = RoomUtil.getRooms().get(roomName).getRoomInfo();
			OutboundMessage message = new OutboundMessage();
			message.setMessage("New occurence at " + incidentNode.get("timestamp"));
			RoomUtil.getBotClient().getMessagesClient().sendMessage(roomInfo.getRoomSystemInfo().getId(), message);
			return;
		}

		// Get user list
		List<UserInfo> impactedUsers = User.getImpactedUsers(incidentNode);

		if (impactedUsers.isEmpty()) {
			System.out.println("No user to notify -> drop incident");
			return;
		}

		Room room = new Room();
		room.setName(roomName);
		room.setDescription("test");
		room.setDiscoverable(true);
		room.setPublic(true);
		room.setViewHistory(true);
		RoomInfo roomInfo = RoomUtil.getBotClient().getStreamsClient().createRoom(room);
		RoomUtil.getRooms().put(roomKey, new RoomWrapper(roomInfo, incidentNode, roomKey));
		if (roomInfo == null) {
			System.out.println("Failed to create room!!");
			return;
		}
		// Add impacted users
		for (UserInfo user : impactedUsers) {
			RoomUtil.getBotClient().getStreamsClient().addMemberToRoom(roomInfo.getRoomSystemInfo().getId(),
					user.getId());
		}

		// Send welcome message
		OutboundMessage message = new OutboundMessage();
		message.setMessage("First occurence at " + incidentNode.get("timestamp"));
		RoomUtil.getBotClient().getMessagesClient().sendMessage(roomInfo.getRoomSystemInfo().getId(), message);

	}

	@Override
	public void run() {
		while (true) {
			try {
				JsonNode incidentNode = messageQueue.take();
				processIncident(incidentNode);
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

}
