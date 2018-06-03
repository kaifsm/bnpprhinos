package chatbot.ai_event.processor.nlp.task;

import chatbot.ai_event.processor.datamodel.RoomUtil;

public class DownloadLogTask extends Task {
	
	@Override
	public void perform() throws Exception {
		
		System.out.println("I am going to download the logs for these systems now - " + systems_);
		RoomUtil.sendMessage(streamId_, "I am going to download the logs for these systems now - " + systems_);
		
		Thread.sleep(2000);
		
		System.out.println("Got it - copied to machineXX:/level2support/log/");
		RoomUtil.sendMessage(streamId_, "Got it - logs are available in machineXX:/level2support/log/");
		
		System.out.println("DownloadLogTask completed :)");
	}
}
