package chatbot.ai_event.processor;

import clients.SymBotClient;
import listeners.RoomListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;
import model.events.*;
import chatbot.ai_event.processor.nlp.RhinosNLP;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoomListenerTestImpl implements RoomListener {

    private SymBotClient botClient;
    
    private RhinosNLP nlpEngine;

    public RoomListenerTestImpl(SymBotClient botClient) throws IOException {
        this.botClient = botClient;
        
    	nlpEngine = new RhinosNLP();
    }

    private final Logger logger = LoggerFactory.getLogger(RoomListenerTestImpl.class);

    public void onRoomMessage(InboundMessage inboundMessage) {
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage("Hi "+inboundMessage.getUser().getFirstName()+"!!!!!");
        try {
            this.botClient.getMessagesClient().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
            
            nlpEngine.parse(inboundMessage.getStream().getStreamId(), inboundMessage.getMessage());
        	
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRoomCreated(RoomCreated roomCreated) {
    	OutboundMessage message = new OutboundMessage();
        message.setMessage("Hello group !");
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
    	//if ( userJoinedRoom.getAffectedUser().getFirstName() == null)
    	//	return;
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage("Welcome "+userJoinedRoom.getAffectedUser().getFirstName()+"!");
        try {
            this.botClient.getMessagesClient().sendMessage(userJoinedRoom.getStream().getStreamId(), messageOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onUserLeftRoom(UserLeftRoom userLeftRoom) {

    }
}
