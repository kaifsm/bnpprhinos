package chatbot.ai_event.processor;

import java.net.URL;

import authentication.SymBotAuth;
import chatbot.ai_event.processor.datamodel.RoomUtil;
import clients.SymBotClient;
import configuration.SymConfig;
import configuration.SymConfigLoader;

public class Rhinos {

	public static void main(String[] args) {
		Rhinos app = new Rhinos();
	}

	Rhinos() {
		
		connectSymphony();
		
		//Main thread process AI messages
		(new AIMsgProcessor()).run();
		
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
