package chatbot.ai_event.processor;

import authentication.SymBotAuth;
import chatbot.ai_event.processor.datamodel.RoomUtil;
import chatbot.ai_event.processor.nlp.RhinosNLP;
import clients.SymBotClient;
import configuration.SymConfig;
import configuration.SymConfigLoader;
import exceptions.SymClientException;
import listeners.IMListener;
import listeners.RoomListener;
import model.*;
import model.events.RoomCreated;
import model.events.RoomDeactivated;
import model.events.RoomMemberDemotedFromOwner;
import model.events.RoomMemberPromotedToOwner;
import model.events.RoomUpdated;
import model.events.UserJoinedRoom;
import model.events.UserLeftRoom;
import services.DatafeedEventsService;
import javax.ws.rs.core.NoContentException;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotExample {

    public static void main(String [] args) {
        BotExample app = new BotExample();
    }
    
    public class TestRoomListener implements RoomListener {
    	
        private SymBotClient botClient;
        
        UserInfo botUserInfo;
        
        private RhinosNLP nlpEngine;

        public TestRoomListener(SymBotClient botClient, UserInfo botUserInfo) throws Exception {
            this.botClient = botClient;
            this.botUserInfo = botUserInfo;
            
            nlpEngine = new RhinosNLP();
        }

		@Override
		public void onRoomCreated(RoomCreated arg0) {
		}

		@Override
		public void onRoomDeactivated(RoomDeactivated arg0) {
		}

		@Override
		public void onRoomMemberDemotedFromOwner(RoomMemberDemotedFromOwner arg0) {
		}

		@Override
		public void onRoomMemberPromotedToOwner(RoomMemberPromotedToOwner arg0) {
		}

		@Override
		public void onRoomMessage(InboundMessage inboundMessage) {
			
			User msgUser = inboundMessage.getUser();
			UserInfo botUser = botClient.getBotUserInfo();
			
			if (inboundMessage.getUser().getUserId().equals(botUserInfo.getId()))
			{
				System.out.println("message coming from myself...ignoring...");
				return;
			}
			
			 try {
				 nlpEngine.parse(inboundMessage.getStream().getStreamId(), inboundMessage.getMessageText());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onRoomReactivated(Stream arg0) {
		}

		@Override
		public void onRoomUpdated(RoomUpdated arg0) {
		}

		@Override
		public void onUserJoinedRoom(UserJoinedRoom arg0) {
		}

		@Override
		public void onUserLeftRoom(UserLeftRoom arg0) {
		}
    }


    public BotExample() {
    	
    	
        URL url = getClass().getClassLoader().getResource("RhinosSymphonyConfig.json");
        SymConfigLoader configLoader = new SymConfigLoader();
        SymConfig config = configLoader.loadFromFile(url.getFile());
        SymBotAuth botAuth = new SymBotAuth(config);
        botAuth.authenticate();
        SymBotClient botClient = SymBotClient.initBot(config, botAuth);
        
        RoomUtil.setBotClient(botClient);
        
        DatafeedEventsService datafeedEventsService = botClient.getDatafeedEventsService();
        
        try {
        	UserInfo botUserInfo = botClient.getUsersClient().getUserFromEmail("bot.user4@example.com", true);
        	
        	RoomListener roomListener = new TestRoomListener(botClient, botUserInfo);
            datafeedEventsService.addRoomListener(roomListener);
            
            IMListener imListener = new IMListenerImpl(botClient);
            datafeedEventsService.addIMListener(imListener);
            
//            createRoom(botClient);
            playWithExistingRoom(botClient);
            
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private void playWithExistingRoom(SymBotClient botClient) throws Exception {
    	String roomStreamId = "kZ1deiFC_9MPirS340Su13___pxf4OgGdA";
    	
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    	
        OutboundMessage outMessage = new OutboundMessage();
        outMessage.setMessage("good afternoon from 2018/06/03 " + sdf.format(Calendar.getInstance().getTime()));
        
//        UserInfo userInfoQC = botClient.getUsersClient().getUserFromEmail("quentin.courtois@asia.bnpparibas.com", true);
//        UserInfo userInfoKS = botClient.getUsersClient().getUserFromEmail("katherine.sung@asia.bnpparibas.com", true);
//        UserInfo userInfoPL = botClient.getUsersClient().getUserFromEmail("paulw.lee@ext.asia.bnpparibas.com", true);
//        
//        botClient.getStreamsClient().addMemberToRoom(roomStreamId,userInfoQC.getId());
//        botClient.getStreamsClient().addMemberToRoom(roomStreamId,userInfoPL.getId());
//        botClient.getStreamsClient().addMemberToRoom(roomStreamId,userInfoKS.getId());
    	
    	try {
			botClient.getMessagesClient().sendMessage(roomStreamId, outMessage);
		} catch (SymClientException e) {
			e.printStackTrace();
		}
    }

    private void createRoom(SymBotClient botClient){
        try {

            UserInfo userInfoQC = botClient.getUsersClient().getUserFromEmail("quentin.courtois@asia.bnpparibas.com", true);
            UserInfo userInfoKS = botClient.getUsersClient().getUserFromEmail("katherine.sung@asia.bnpparibas.com", true);
            UserInfo userInfoPL = botClient.getUsersClient().getUserFromEmail("paulw.lee@ext.asia.bnpparibas.com", true);
            //get user IM and send message
            String IMStreamIdQC = botClient.getStreamsClient().getUserIMStreamId(userInfoQC.getId());
            //String IMStreamIdKS = botClient.getStreamsClient().getUserIMStreamId(userInfoKS.getId());
//            String IMStreamIdPL = botClient.getStreamsClient().getUserIMStreamId(userInfoPL.getId());
            //OutboundMessage message = new OutboundMessage();
          //  message.setMessage("Hello QQ");
         //   botClient.getMessagesClient().sendMessage(IMStreamIdQC, message);
//            botClient.getMessagesClient().sendMessage(IMStreamIdKS, message);
//            botClient.getMessagesClient().sendMessage(IMStreamIdPL, message);

            Room room = new Room();
            room.setName("Rhinos test 20180602 ");
            room.setDescription("test");
            room.setDiscoverable(true);
            room.setPublic(true);
            room.setViewHistory(true);
            RoomInfo roomInfo = botClient.getStreamsClient().createRoom(room);
            if (roomInfo == null)
            {
            	System.out.println("Failed to create room!!");
            	return;
            }
            botClient.getStreamsClient().addMemberToRoom(roomInfo.getRoomSystemInfo().getId(),userInfoQC.getId());
            botClient.getStreamsClient().addMemberToRoom(roomInfo.getRoomSystemInfo().getId(),userInfoKS.getId());
            botClient.getStreamsClient().addMemberToRoom(roomInfo.getRoomSystemInfo().getId(),userInfoPL.getId());
            
            //botClient.getStreamsClient().activateRoom(roomInfo.getRoomSystemInfo().getId());

            OutboundMessage message2 = new OutboundMessage();
            message2.setMessage("Hello room members !");
            botClient.getMessagesClient().sendMessage(roomInfo.getRoomSystemInfo().getId(), message2);

            //Room newRoomInfo = new Room();
            //newRoomInfo.setName("test generator");
            //botClient.getStreamsClient().updateRoom(roomInfo.getRoomSystemInfo().getId(),newRoomInfo);

            //List<RoomMember> members =  botClient.getStreamsClient().getRoomMembers(roomInfo.getRoomSystemInfo().getId());

           //botClient.getStreamsClient().promoteUserToOwner(roomInfo.getRoomSystemInfo().getId(), userInfoKS.getId());

//            botClient.getStreamsClient().deactivateRoom(roomInfo.getRoomSystemInfo().getId());


        } catch (NoContentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
