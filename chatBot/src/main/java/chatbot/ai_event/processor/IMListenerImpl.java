package chatbot.ai_event.processor;
import clients.SymBotClient;
import listeners.IMListener;
import model.InboundMessage;
import model.OutboundMessage;
import model.Stream;

public class IMListenerImpl implements IMListener {

    private SymBotClient botClient;

    public IMListenerImpl(SymBotClient botClient) {
        this.botClient = botClient;
    }

    public void onIMMessage(InboundMessage inboundMessage) {
    	if (inboundMessage.getUser().getUsername().equalsIgnoreCase("bot.user4"))
    		return;
        OutboundMessage messageOut = new OutboundMessage();
        messageOut.setMessage("What mean "+inboundMessage.getMessage());
        try {
            this.botClient.getMessagesClient().sendMessage(inboundMessage.getStream().getStreamId(), messageOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onIMCreated(Stream stream) {

    }
}
