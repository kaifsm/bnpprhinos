package chatbot.ai_event.processor.nlp.task;

import chatbot.ai_event.processor.datamodel.RoomUtil;

public class CreateServiceNowTicketTask extends Task {

	@Override
	public void perform() throws Exception {
		RoomUtil.createServiceNowTicket(streamId_);
	}

}
