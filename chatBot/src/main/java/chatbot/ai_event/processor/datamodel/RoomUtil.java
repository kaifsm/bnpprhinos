package chatbot.ai_event.processor.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;

import clients.SymBotClient;
import exceptions.SymClientException;
import model.OutboundMessage;
import model.RoomInfo;

public class RoomUtil {

	static SymBotClient botClient;
	static Map<String, RoomWrapper> rooms = new HashMap<String, RoomWrapper>();
	static Integer serviceNowTicketCounter = 0;

	public static Map<String, RoomWrapper> getRooms() {
		return rooms;
	}

	public static SymBotClient getBotClient() {
		return botClient;
	}

	public static void setBotClient(SymBotClient botClient) {
		RoomUtil.botClient = botClient;
	}

	public static void closeRoom(String streamId) {

	}

	static List<String> mergingCriterias = new ArrayList<String>();

	public static void addMergingCriteria(String criteria) {
		mergingCriterias.add(criteria);
	}

	public static String getRoomKey(JsonNode incident) {
		StringBuffer name = new StringBuffer();
		for (String mergingCriteria : mergingCriterias) {
			if (incident.has(mergingCriteria)) {
				if (incident.get(mergingCriteria).isArray()) {
					Consumer<JsonNode> myConsumer = (JsonNode mergingCriteriaItem) -> name
							.append(mergingCriteriaItem.asText()).append(" ");
					incident.get(mergingCriteria).forEach(myConsumer);
				} else {
					name.append(incident.get(mergingCriteria).asText()).append(" ");
				}
			}
		}
		return name.toString().trim();
	}

	public static void sendMessage(String streamId, String msg) throws SymClientException {
		OutboundMessage messageOut = new OutboundMessage();
		messageOut.setMessage(msg);
		botClient.getMessagesClient().sendMessage(streamId, messageOut);
	}

	public static String getserviceNowTicketId() {
		synchronized (serviceNowTicketCounter) {
			return "ITM" + String.format("%06d", ++serviceNowTicketCounter);
		}
	}

	public static void createServiceNowTicket(String streamId) throws SymClientException {
		// botClient.getBotClient().getStreamsClient().getRoomInfo(streamId);
		OutboundMessage messageOut = new OutboundMessage();
		messageOut.setMessage("Service now ticket created " + getserviceNowTicketId());
		botClient.getMessagesClient().sendMessage(streamId, messageOut);
	}
}
