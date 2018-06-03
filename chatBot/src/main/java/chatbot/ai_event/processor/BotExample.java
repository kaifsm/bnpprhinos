package chatbot.ai_event.processor;
import authentication.SymBotAuth;
import clients.SymBotClient;
import configuration.SymConfig;
import configuration.SymConfigLoader;
import listeners.IMListener;
import listeners.RoomListener;
import model.*;
import services.DatafeedEventsService;
import javax.ws.rs.core.NoContentException;

import java.net.URL;
import java.util.List;

public class BotExample {

    public static void main(String [] args) {
        BotExample app = new BotExample();
    }


    public BotExample() {
        //URL url = getClass().getResource("C:\\MisterQ\\java\\rhinos\\src\\main\\resources\\RhinosSymphonyConfig.json");
        SymConfigLoader configLoader = new SymConfigLoader();
        SymConfig config = configLoader.loadFromFile("C:\\MisterQ\\java\\rhinos\\src\\main\\resources\\RhinosSymphonyConfig.json");
        SymBotAuth botAuth = new SymBotAuth(config);
        botAuth.authenticate();
        SymBotClient botClient = SymBotClient.initBot(config, botAuth);
        //DatafeedEventsService datafeedEventsService = botClient.getDatafeedEventsService();
        //RoomListener roomListenerTest = new RoomListenerTestImpl(botClient);
        //datafeedEventsService.addRoomListener(roomListenerTest);
        //IMListener imListener = new IMListenerImpl(botClient);
        //datafeedEventsService.addIMListener(imListener);
        createRoom(botClient);
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
