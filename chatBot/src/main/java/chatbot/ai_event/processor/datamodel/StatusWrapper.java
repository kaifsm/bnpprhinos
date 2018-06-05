package chatbot.ai_event.processor.datamodel;

public class StatusWrapper {
	
	public StatusWrapper(long ts, String status) {
		super();
		this.ts = ts;
		this.status = status;
	}
	long ts;
	String status;

}
