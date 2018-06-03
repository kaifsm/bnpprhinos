package chatbot.ai_event.processor;

import java.net.URL;

import javax.ws.rs.core.NoContentException;

import authentication.SymBotAuth;
import chatbot.ai_event.processor.BotExample.TestRoomListener;
import chatbot.ai_event.processor.datamodel.RoomUtil;
import clients.SymBotClient;
import configuration.SymConfig;
import configuration.SymConfigLoader;
import exceptions.SymClientException;
import listeners.IMListener;
import listeners.RoomListener;
import model.UserInfo;
import services.DatafeedEventsService;

import static spark.Spark.*;
import spark.Request;
import spark.Response;
import spark.Route;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;

public class Rhinos {

	public static void main(String[] args) {
		Rhinos app = new Rhinos();
	}

	Rhinos() {

		try {
			connectSymphony();

			new Thread(() -> {
				post("/rhinos/issues", (req, res) -> {
					JsonNode issue = JsonLoader.fromString(res.body());
					AIMsgProcessor.messageQueue.put(issue);
					res.status(201);
					return null;
				});
			}).start();

			// Main thread process AI messages
			(new AIMsgProcessor()).run();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	void connectSymphony() throws Exception {
		URL url = getClass().getClassLoader().getResource("RhinosSymphonyConfig.json");
		SymConfigLoader configLoader = new SymConfigLoader();
		SymConfig config = configLoader.loadFromFile(url.getPath());
		SymBotAuth botAuth = new SymBotAuth(config);
		botAuth.authenticate();
		System.out.println("Connection to Symphony successful !");

		SymBotClient botClient = SymBotClient.initBot(config, botAuth);
		RoomUtil.setBotClient(botClient);

		UserInfo botUserInfo = botClient.getUsersClient().getUserFromEmail(config.getBotEmailAddress(), true);

		DatafeedEventsService datafeedEventsService = botClient.getDatafeedEventsService();

		RoomListener roomListener = new RoomListenerImpl(botClient, botUserInfo);
		datafeedEventsService.addRoomListener(roomListener);

		IMListener imListener = new IMListenerImpl(botClient);
		datafeedEventsService.addIMListener(imListener);
	}
}
