
import java.net.URL;

import authentication.SymBotAuth;
import clients.SymBotClient;
import configuration.SymConfig;
import configuration.SymConfigLoader;

public class Rhinos {

	public static void main(String[] args) {
		Rhinos app = new Rhinos();
	}

	Rhinos() {
		SymBotClient botClient = connectSymphony();

		(new AIMsgProcessor(botClient)).run();
		
		//finished = false;
		//while (!finished)
		//{
			//Thread.sleep(1000000000);
		//}
	}

	SymBotClient connectSymphony() {
		URL url = getClass().getResource("RhinosSymphonyConfig.json");
		SymConfigLoader configLoader = new SymConfigLoader();
		SymConfig config = configLoader.loadFromFile(url.getPath());
		SymBotAuth botAuth = new SymBotAuth(config);
		botAuth.authenticate();
		System.out.println("Connection to Symphony successful !");
		return SymBotClient.initBot(config, botAuth);
	}

}
