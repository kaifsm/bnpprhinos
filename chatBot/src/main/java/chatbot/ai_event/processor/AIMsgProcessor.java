package chatbot.ai_event.processor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;

import chatbot.ai_event.processor.datamodel.Filter;
import chatbot.ai_event.processor.datamodel.Profile;
import chatbot.ai_event.processor.datamodel.RoomUtil;
import chatbot.ai_event.processor.datamodel.RoomWrapper;
import chatbot.ai_event.processor.datamodel.User;
import clients.symphony.api.constants.CommonConstants;
import clients.symphony.api.constants.PodConstants;
import exceptions.SymClientException;
import exceptions.UnauthorizedException;
import model.OutboundMessage;
import model.Room;
import model.RoomInfo;
import model.UserInfo;

public class AIMsgProcessor implements Runnable {

	public static LinkedBlockingQueue<JsonNode> messageQueue = new LinkedBlockingQueue<JsonNode>();

	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

	public static void main(String[] args) {
		Rhinos app = new Rhinos(true);
		// AIMsgProcessor app = new AIMsgProcessor();
		try {
			//File file = new File("C:\\Users\\Hack-1\\bnpprhinos\\chatBot\\src\\main\\resources\\RepeatedCancels.json");
			File file = new File("C:\\Users\\Hack-1\\bnpprhinos\\chatBot\\src\\main\\resources\\MarketDataSlowness.json");
			//File file = new File("C:\\Users\\Hack-1\\bnpprhinos\\chatBot\\src\\main\\resources\\NetworkDown.json");
			JsonNode incidentNode = JsonLoader.fromFile(file);
			messageQueue.put(incidentNode);

			Thread.sleep(10000);
			//messageQueue.put(incidentNode);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void processIncidentFromJsonFile() throws IOException, SymClientException {
		URL url = getClass().getClassLoader().getResource("IssueSample.json");
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
		URL url = getClass().getClassLoader().getResource("RhinosUsers&Profiles.json");
		System.out.println("Loading users & profiles from file " + url.getPath());
		JsonNode allNodes = JsonLoader.fromURL(url);

		// load filter criterias
		if (!allNodes.has("Incident attributes")) {
			System.out.println("Mssing Incident attributes !!!");
			throw new Exception("Missing Incident attributes !!!");
		}
		Consumer<JsonNode> criteriaConsumer = (JsonNode criteriaNode) -> Filter
				.addIncidentAttribute(criteriaNode.asText());
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
		String roomName;
		String roomKey = RoomUtil.getRoomKey(incidentNode);
		if (roomKey.startsWith("MarketDataSlowness"))
			roomName = "MarketDataSlowness " + LocalDateTime.now().format(formatter);
		else {
			roomName = roomKey + " " + LocalDateTime.now().format(formatter);
		}

		// Check if room already exist
		if (RoomUtil.getRooms().containsKey(roomKey)) {
			System.out.println("Update existing room " + roomKey);
			RoomInfo roomInfo = RoomUtil.getRooms().get(roomKey).getRoomInfo();
			OutboundMessage message = new OutboundMessage();
			message.setMessage("Another occurence at " + incidentNode.get("timestamp"));
			RoomUtil.getBotClient().getMessagesClient().sendMessage(roomInfo.getRoomSystemInfo().getId(), message);
			return;
		}

		// Get user list
		Set<String> impactedProfiles = new HashSet<String>();
		List<UserInfo> impactedUsers = User.getImpactedUsers(incidentNode, impactedProfiles);

		if (impactedUsers.isEmpty()) {
			System.out.println("No user to notify -> drop incident");
			return;
		}
		System.out.println("Create new room " + roomKey);
		Room room = new Room();
		room.setName(roomName);
		room.setDescription(roomName);
		room.setDiscoverable(true);
		room.setPublic(true);
		room.setViewHistory(true);
		RoomInfo roomInfo = null;
		try {
			roomInfo = RoomUtil.getBotClient().getStreamsClient().createRoom(room);
		} catch (IllegalStateException e) {
			System.out.println("Failed to create room ! already exist ?");
			// roomInfo = createRoom(room);
		}
		if (roomInfo == null) {
			System.out.println("Failed to create room, already exist ?");
			return;
			// roomInfo = createRoom(room);
		}
		RoomUtil.getRooms().put(roomKey, new RoomWrapper(roomInfo, incidentNode, roomKey));
		// Add impacted users
		for (UserInfo user : impactedUsers) {
			System.out.println("Add user " + user.getEmailAddress());
			RoomUtil.getBotClient().getStreamsClient().addMemberToRoom(roomInfo.getRoomSystemInfo().getId(),
					user.getId());
		}

		// Send welcome message
		OutboundMessage message = new OutboundMessage();
		StringBuilder messageBuffer = new StringBuilder();
		messageBuffer.append("<table>");
		messageBuffer.append("<tr><td>Issue Type</td><td>").append(incidentNode.get("issue type").asText()).append("</td></tr>");
		if (incidentNode.get("timestamp")!= null)
			messageBuffer.append("<tr><td>Time of occurence</td><td>").append(prettryPrint(incidentNode.get("timestamp"))).append("</td></tr>");
		if (incidentNode.get("impacted systems")!= null)
			messageBuffer.append("<tr><td>Concerned Systems</td><td>").append(prettryPrint(incidentNode.get("impacted systems"))).append("</td></tr>");
		if (incidentNode.get("impacted flows")!= null)
			messageBuffer.append("<tr><td>Impacted Flow</td><td>").append(prettryPrint(incidentNode.get("impacted flows"))).append("</td></tr>");
		if (incidentNode.get("impacted clients")!= null)
			messageBuffer.append("<tr><td>Impacted Clients</td><td>").append(prettryPrint(incidentNode.get("impacted clients"))).append("</td></tr>");
		if (incidentNode.get("impacted markets")!= null)
			messageBuffer.append("<tr><td>Impacted Markets</td><td>").append(prettryPrint(incidentNode.get("impacted markets"))).append("</td></tr>");
		if (incidentNode.get("pnl")!= null)
		messageBuffer.append("<tr><td>Estimated PnL</td><td> EUR ").append(prettryPrint(incidentNode.get("pnl"))).append("</td></tr>");
		messageBuffer.append("</table><br />");
		message.setMessage(messageBuffer.toString());
		//message.
		//message.setMessage(printIncidentDescription(incidentNode));
		// message.setMessage("First occurence at " + incidentNode.get("timestamp"));
		RoomUtil.getBotClient().getMessagesClient().sendMessage(roomInfo.getRoomSystemInfo().getId(), message);

		// Send list of involved teams
		message = new OutboundMessage();
		StringBuilder msgBuf = new StringBuilder().append("Adding following teams in this chat room:\n");
		boolean firstAdd = true;
		for (String profileName : impactedProfiles) {
			if (firstAdd) {
				msgBuf.append(profileName);
				firstAdd = false;
			} else
				msgBuf.append(", ").append(profileName);
		}
		message.setMessage(msgBuf.toString());
		RoomUtil.getBotClient().getMessagesClient().sendMessage(roomInfo.getRoomSystemInfo().getId(), message);
	}
	
	/*protected static String formatJsonNodeValue(JsonNode jsonNode) {
		if (jsonNode.isTextual())
		{
			return jsonNode.asText();
		}
		if (jsonNode.isArray())
		{
			
		}
	}*/

	static String prettryPrint(JsonNode incidentNode) {
		/*
		 * StringBuffer description = new StringBuffer(); Consumer<JsonNode>
		 * incidentAttibuteConsumer = (JsonNode incidentAttibute) -> description
		 * .append(incidentAttibute.asText()).append(":");
		 * //.append(incidentNode.get(incidentAttibute.asText()).asText()).append("\n");
		 * incidentNode.forEach(incidentAttibuteConsumer); return
		 * description.toString();
		 */
		try {
			ObjectMapper mapper = new ObjectMapper();
			Object json = mapper.readValue(incidentNode.toString(), Object.class);
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
		} catch (Exception e) {
			return "Sorry, pretty print didn't work";
		}

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

	public RoomInfo createRoom(Room room) throws SymClientException {
		Response response = RoomUtil.getBotClient().getPodClient()
				.target(CommonConstants.HTTPSPREFIX + RoomUtil.getBotClient().getConfig().getPodHost() + ":"
						+ RoomUtil.getBotClient().getConfig().getPodPort())
				.path(PodConstants.CREATEROOM).request(MediaType.APPLICATION_JSON)
				.header("sessionToken", RoomUtil.getBotClient().getSymAuth().getSessionToken())
				.post(Entity.entity(room, MediaType.APPLICATION_JSON));
		RoomInfo roomInfo = response.readEntity(RoomInfo.class);
		// if (response.getStatusInfo().getFamily() !=
		// Response.Status.Family.SUCCESSFUL) {
		// try {
		// handleError(response, RoomUtil.getBotClient());
		// } catch (UnauthorizedException ex){
		// return createRoom(room);
		// }
		// return null;
		// }
		return roomInfo;
	}

}
