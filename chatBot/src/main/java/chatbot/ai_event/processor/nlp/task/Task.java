package chatbot.ai_event.processor.nlp.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class Task {
	
	protected String streamId_;
	
	protected List<String> systems_ = new ArrayList<>();
	
	public Task() {
		System.out.println("Task created - " + this.getClass());
	}
	
	public void addStreamId(String streamId) {
		streamId_ = streamId;	
	}
	
	public void addSystems(Collection<String> systems) {
		systems_.addAll(systems);
	}
	
	public abstract void perform() throws Exception;
}
