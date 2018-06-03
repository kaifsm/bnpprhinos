package chatbot.ai_event.processor.nlp.task;

import chatbot.ai_event.processor.datamodel.RoomUtil;

public class SendEmailUpdateTask extends Task {

	@Override
	public void perform() throws Exception {
		System.out.println("I am now preparing the email update to be sent out to concerned parties...");
		RoomUtil.sendMessage(streamId_, "I am now preparing the email update to be sent out to concerned parties...");
		
		Thread.sleep(2000);
		
		System.out.println("Email sent out now!");
		RoomUtil.sendMessage(streamId_, "Email sent out now!");
	}
}
