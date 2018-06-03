package chatbot.ai_event.processor.datamodel;

import com.fasterxml.jackson.databind.JsonNode;

import model.RoomInfo;

public class RoomWrapper {
	
	public RoomInfo getRoomInfo() {
		return roomInfo;
	}
	public void setRoomInfo(RoomInfo roomInfo) {
		this.roomInfo = roomInfo;
	}
	public JsonNode getIncidentNode() {
		return incidentNode;
	}
	public void setIncidentNode(JsonNode incidentNode) {
		this.incidentNode = incidentNode;
	}
	
	public String getRoomKey() {
		return roomKey;
	}
	
	public RoomWrapper(RoomInfo roomInfo, JsonNode incidentNode, String roomKey) {
		super();
		this.roomInfo = roomInfo;
		this.incidentNode = incidentNode;
		this.roomKey = roomKey;
	}

	RoomInfo roomInfo;
	JsonNode incidentNode;
	String roomKey;

}
