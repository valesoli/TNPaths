package ar.edu.itba.records.utils;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeValueVisitor implements Result.ResultVisitor<Exception> {

    private final Map<String, List<Node>> attributes = new HashMap<>();


    @Override
    public boolean visit(Result.ResultRow row) {
        String attributeName = row.getString("attribute");
        Node valueNode = row.getNode("value");
        List<Node> valueNodesForAttribute = attributes.getOrDefault(attributeName, new ArrayList<>());
        valueNodesForAttribute.add(valueNode);
        attributes.put(attributeName, valueNodesForAttribute);
        return true;
    }

    public Map<String, List<Node>> getAttributes() {
        return attributes;
    }
}
