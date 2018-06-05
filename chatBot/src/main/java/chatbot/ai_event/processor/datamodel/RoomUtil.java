package chatbot.ai_event.processor.datamodel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

	public static void setBotClient(SymBotClient botClient_) {
		botClient = botClient_;
	}


	public static void sendMessage(String streamId, String msg) throws SymClientException {
		OutboundMessage messageOut = new OutboundMessage();
		messageOut.setMessage(msg);
		botClient.getMessagesClient().sendMessage(streamId, messageOut);
	}
	
	public static void closeRoom(String streamId) throws SymClientException {
		
		//Remove room from the bot cache
		Iterator<Entry<String, RoomWrapper>> itr = rooms.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, RoomWrapper> roomEntry = itr.next();
			RoomWrapper room = roomEntry.getValue();
			if (room.getRoomInfo().getRoomSystemInfo().getId().equals(streamId) && room.getTicket() != null) {
				//Close incident ticket
				String ticketNo = "ITM" + String.format("%06d", serviceNowTicketCounter);
				sendMessage(streamId, "Updating and resolving incident ticket " + ticketNo);
				break;
			}
		}
		
		//Deactivate in Symphony
		sendMessage(streamId, "Closing room now");
		botClient.getStreamsClient().deactivateRoom(streamId);
		
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

	public static String getserviceNowTicketId() {
		synchronized (serviceNowTicketCounter) {
			return "ITM" + String.format("%06d", ++serviceNowTicketCounter);
		}
	}

	public static void createServiceNowTicket(String streamId) throws SymClientException {
		// botClient.getBotClient().getStreamsClient().getRoomInfo(streamId);
		String ticket = getserviceNowTicketId();
		sendMessage(streamId, "Service now ticket created " + ticket);
		
		//Update ticket in room
		for (RoomWrapper room : rooms.values()) {
			if (room.getRoomInfo().getRoomSystemInfo().getId().equals(streamId)) {
				room.setTicket(ticket);
			}
		}
	}
	
	static RoomWrapper getRoom(String id)
	{
		for (RoomWrapper room : rooms.values()) {
			if (room.getRoomInfo().getRoomSystemInfo().getId().equals(id)) {
				return room;
			}
		}
		return null;
		
	}
	static public void updateStatus(String streamId_, String rawMessage_)
	{
		if (getRoom(streamId_)!= null) 
		{
				getRoom(streamId_).setStatus(rawMessage_);
		}
	}
	
	static public void publishStatus(String streamId_) throws SymClientException
	{
//		if (getRoom(streamId_)!= null) 
//		{
//			if (getRoom(streamId_).getStatus() == null)
//				sendMessage(streamId_, "Good question....I don't know...hm...Anyone?");
//			else
//				sendMessage(streamId_, getRoom(streamId_).getStatus());
//		}
		
		publishStatusHistory(streamId_);
	}
		
	static public void publishStatusHistory(String streamId_) throws SymClientException
	{
		if (getRoom(streamId_)!= null) 
		{
			if (getRoom(streamId_).getStatusList() == null || getRoom(streamId_).getStatusList().isEmpty())
				sendMessage(streamId_, "Good question....I don't know...hm...Anyone?");
			else {
				StringBuilder messageBuffer = new StringBuilder();
				messageBuffer.append("<table>");
				messageBuffer.append("<tr><th>Timestamp</th><th>Audit Trail</th></tr>");
				for (StatusWrapper status : getRoom(streamId_).getStatusList() ) {
					messageBuffer.append("<tr><td>").append(new SimpleDateFormat("HH:mm:ss").format(new Date(status.ts))).append("</td><td>");
					messageBuffer.append(status.status).append("</td></tr>");
				}
				messageBuffer.append("</table>");
				
				sendMessage(streamId_, messageBuffer.toString());
				
			}
		}
	}
}
