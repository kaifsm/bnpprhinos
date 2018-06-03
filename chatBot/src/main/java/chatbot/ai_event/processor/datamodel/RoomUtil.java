package chatbot.ai_event.processor.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;

import clients.SymBotClient;
import exceptions.SymClientException;
import model.OutboundMessage;

public class RoomUtil {
	
	static SymBotClient botClient;

	public static SymBotClient getBotClient() {
		return botClient;
	}

	public static void setBotClient(SymBotClient botClient) {
		RoomUtil.botClient = botClient;
	}

	static List<String> mergingCriterias = new ArrayList<String>();

	public static void addMergingCriteria(String criteria) {
		mergingCriterias.add(criteria);
	}

	public static String getRoomName(JsonNode incident) {
		StringBuffer name = new StringBuffer().append("Alert ");
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
}
