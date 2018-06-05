package chatbot.ai_event.processor.datamodel;

import java.util.ArrayList;
import java.util.List;

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
		
		//StatusWrapper st = new StatusWrapper(System.currentTimeMillis(), "Room created");
		//statusList.add(st);
	}

	RoomInfo roomInfo;
	JsonNode incidentNode;
	String roomKey;
	String ticket = null;
	String status = null;
	List<StatusWrapper> statusList = new ArrayList<StatusWrapper>();
	
	public List<StatusWrapper> getStatusList() {
		return statusList;
	}
	public void setStatusList(List<StatusWrapper> statusList) {
		this.statusList = statusList;
	}
	
	RoomWrapper()
	{
		StatusWrapper st = new StatusWrapper(System.currentTimeMillis(), "Room created");
		statusList.add(st);
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
		String statusInHistory = status.substring("the latest status is".length());
		StatusWrapper st = new StatusWrapper(System.currentTimeMillis(), "Changed status to:" + statusInHistory);
		statusList.add(st);
	}
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
		StatusWrapper st = new StatusWrapper(System.currentTimeMillis(), "Created servicenow ticet:" + ticket);
		statusList.add(st);
	}

}
