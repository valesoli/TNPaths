package ar.edu.itba.records.utils;

import org.neo4j.graphdb.Node;

import java.util.*;

public class AllAttributesSerializer implements AttributesSerializer {

    @Override
    public Map<String, Object> serialize(Map<String, List<Node>> attributes) {
        Map<String, Object> serializedAttributes = new LinkedHashMap<>();

        for (String attributeName : attributes.keySet()) {
            List<Node> valueNodes = attributes.get(attributeName);

            List<Map<String, Object>> values = new ArrayList<>();
            for (Node node : valueNodes) {
                Map<String, Object> attributeObject = new HashMap<>(node.getAllProperties());
                values.add(attributeObject);
            }

            serializedAttributes.put(attributeName, values);
        }
        return serializedAttributes;
    }

	/*@Override
	public Map<String, Object> serializeSensor(Map<String, List<Node>> attributes) {
		// TODO Auto-generated method stub
		return null;
	}*/
}
