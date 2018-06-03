package chatbot.ai_event.processor.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import java.lang.Double;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class Filter {

	static List<String> criterias = new ArrayList<String>();

	Map<String, List<String>> _filter;

	Filter(JsonNode filterNode) {

		_filter = new HashMap<String, List<String>>();
		Consumer<String> criteriaConsumer = (String criteria) -> _filter.put(criteria,
				loadCriteria(criteria, filterNode));

		criterias.forEach(criteriaConsumer);
	}

	List<String> loadCriteria(String criteria, JsonNode filterNode) {
		List<String> result = new ArrayList<String>();
		JsonNode criteriaNode = filterNode.get(criteria);
		if (criteriaNode != null) {
			Consumer<JsonNode> criteriaValueConsumer = (JsonNode criteriaValue) -> result.add(criteriaValue.asText());
			criteriaNode.forEach(criteriaValueConsumer);
		}
		return result;
	}

	boolean match(JsonNode incidentNode) {
		for (Map.Entry<String, List<String>> pair : _filter.entrySet()) {
			if (pair.getValue().isEmpty())
				continue;
			String criteria = pair.getKey();

			if (incidentNode.has(criteria)) {
				boolean haveAmatch = false;
				JsonNode incidentCriteriaNode = incidentNode.get(criteria);
				if (incidentCriteriaNode.isArray()) {
					ArrayNode impacts = (ArrayNode) incidentCriteriaNode;
					for (String criteriaValue : pair.getValue()) {
						if (impacts.has(criteriaValue)) {
							haveAmatch = true;
							break;
						}
					}
				} else if (incidentCriteriaNode.isInt()) {
					// Must be the pnl
					Double pnl = Double.valueOf(pair.getValue().get(0));
					if (incidentCriteriaNode.asInt() > pnl)
						haveAmatch = true;
				} else {
					for (String criteriaValue : pair.getValue()) {
						if (criteriaValue.equalsIgnoreCase(incidentCriteriaNode.asText())) {
							haveAmatch = true;
							break;
						}
					}
				}
				if (!haveAmatch) {
					return false;
				}
			}
		}
		return true;
	}

	public static void addCriteria(String criteria) {
		criterias.add(criteria);
	}

	public static List<String> getCriterias() {
		return criterias;
	}

}
