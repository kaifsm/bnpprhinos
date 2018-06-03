package chatbot.ai_event.processor.nlp.task;

import chatbot.ai_event.processor.datamodel.RoomUtil;

public class CloseIncidentTask extends Task {

	@Override
	public void perform() throws Exception {
		RoomUtil.closeRoom(streamId_);
	}

}
