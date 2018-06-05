package chatbot.ai_event.processor;

import clients.SymBotClient;
import exceptions.SymClientException;
import listeners.RoomListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import model.UserInfo;
import model.events.*;
import chatbot.ai_event.processor.nlp.RhinosNLP;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomListenerImpl implements RoomListener {

	private SymBotClient botClient;

	private UserInfo botUserInfo;

	private RhinosNLP nlpEngine;

	public RoomListenerImpl(SymBotClient botClient, UserInfo botUserInfo) throws IOException {
		this.botClient = botClient;

		this.botUserInfo = botUserInfo;

		nlpEngine = new RhinosNLP();
	}

	private final Logger logger = LoggerFactory.getLogger(RoomListenerImpl.class);

	public void onRoomMessage(InboundMessage inboundMessage) {
		if (inboundMessage.getUser().getUserId().equals(botUserInfo.getId())) {
			System.out.println("message coming from myself...ignoring...");
			return;
		}

		try {
			//if (inboundMessage.getMessageText().startsWith("@bot")) {
				nlpEngine.parse(inboundMessage.getStream().getStreamId(), inboundMessage.getMessageText());
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onRoomCreated(RoomCreated roomCreated) {
		OutboundMessage message = new OutboundMessage();
		message.setMessage("Alert !");
		try {
			botClient.getMessagesClient().sendMessage(roomCreated.getStream().getStreamId(), message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onRoomDeactivated(RoomDeactivated roomDeactivated) {

	}

	public void onRoomMemberDemotedFromOwner(RoomMemberDemotedFromOwner roomMemberDemotedFromOwner) {

	}

	public void onRoomMemberPromotedToOwner(RoomMemberPromotedToOwner roomMemberPromotedToOwner) {

	}

	public void onRoomReactivated(Stream stream) {

	}

	public void onRoomUpdated(RoomUpdated roomUpdated) {

	}

	public void onUserJoinedRoom(UserJoinedRoom userJoinedRoom) {
		// if ( userJoinedRoom.getAffectedUser().getFirstName() == null)
		// return;
		OutboundMessage messageOut = new OutboundMessage();
		messageOut.setMessage("Welcome " + userJoinedRoom.getAffectedUser().getFirstName() + "!");
		try {
			this.botClient.getMessagesClient().sendMessage(userJoinedRoom.getStream().getStreamId(), messageOut);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onUserLeftRoom(UserLeftRoom userLeftRoom) {

	}
}
