package chatbot.ai_event.processor.nlp.task;

import chatbot.ai_event.processor.datamodel.RoomUtil;

public class UpdateIncidentStatusTask extends Task {

	@Override
	public void perform() throws Exception {
		RoomUtil.updateStatus(streamId_, rawMessage_);
	}

}
