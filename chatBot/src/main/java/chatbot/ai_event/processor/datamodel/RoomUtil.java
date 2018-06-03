package chatbot.ai_event.processor.datamodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class RoomUtil {

	static List<String> mergingCriterias = new ArrayList<String>();

	public static void addMergingCriteria(String criteria) {
		mergingCriterias.add(criteria);
	}

	public static String getRoomName(JsonNode incident) {
		StringBuffer name = new StringBuffer().append("Alert ");
		for (String mergingCriteria : mergingCriterias) {
			if (incident.has(mergingCriteria)) {
				if (incident.get(mergingCriteria).isArray()) {
					Consumer<JsonNode> myConsumer = (JsonNode mergingCriteriaItem) -> name
							.append(mergingCriteriaItem.asText()).append(" ");
					incident.get(mergingCriteria).forEach(myConsumer);
				} else {
					name.append(incident.get(mergingCriteria).asText()).append(" ");
				}
			}
		}
		return name.toString().trim();
	}
}
