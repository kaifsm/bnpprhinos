package chatbot.ai_event.processor;

import java.net.URL;


import authentication.SymBotAuth;
import chatbot.ai_event.processor.AIMsgProcessor;
import chatbot.ai_event.processor.datamodel.RoomUtil;
import clients.SymBotClient;
import configuration.SymConfig;
import configuration.SymConfigLoader;

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
		connectSymphony();
		new Thread(() -> {
			post("/rhinos/issues", (req, res) -> {
				JsonNode issue = JsonLoader.fromString(res.body());
				AIMsgProcessor.messageQueue.put(issue);
				res.status(201);
				return null;
			});
		}).start();
		
		//Main thread process AI messages
		(new AIMsgProcessor()).run();
		
	}

	private void post(String string, Route route) {
		// TODO Auto-generated method stub
		
	}

	void connectSymphony() {
		URL url = getClass().getResource("RhinosSymphonyConfig.json");
		SymConfigLoader configLoader = new SymConfigLoader();
		SymConfig config = configLoader.loadFromFile(url.getPath());
		SymBotAuth botAuth = new SymBotAuth(config);
		botAuth.authenticate();
		System.out.println("Connection to Symphony successful !");
		RoomUtil.setBotClient( SymBotClient.initBot(config, botAuth) );
	}
}
