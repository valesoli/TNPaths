package ar.edu.itba.records.utils;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.path.IdOnlyNodePath;
import ar.edu.itba.algorithms.utils.path.NodePath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NodeSerialization {

    public static Map<String, Object> fromNode(Node node, AttributesSerializer attributesSerializer,
                                               GraphDatabaseService db) {
        Map<String, Object> serialization = new LinkedHashMap<>(node.getAllProperties());
        Result result = getNodeAttributes(node.getId(), db);
        serialization.put("attributes", serializeAttributes(result, attributesSerializer));
        serialization.put("_id", node.getId());
        serialization.put("id", node.getProperty("id"));
        return serialization;
    }
    
    public static Map<String, Object> fromSensorNode(Node node, AttributesSerializer attributesSerializer,
            GraphDatabaseService db) {
	Map<String, Object> serialization = new LinkedHashMap<>();
	serialization.put("vhas", node.getProperty("vhas"));
	serialization.put("name", node.getProperty("name"));
	System.out.print("fromSensorNode");
	System.out.println(attributesSerializer.toString());
	return serialization;
	}
    
    public static Map<String, Object> fromNotSensorNode(Node node, AttributesSerializer attributesSerializer,
            GraphDatabaseService db) {
	Map<String, Object> serialization = new LinkedHashMap<>();
	serialization.put("vhas", node.getProperty("vhas"));
	System.out.print("fromNotSensorNode");
	System.out.println(attributesSerializer.toString());
	return serialization;
	}
    
    public static Map<String, Object> fromNodeIdProperty(Long id, AttributesSerializer attributesSerializer,
    GraphDatabaseService db) {
        Node node = db.findNode(Label.label("Object"), "id", id);
        Map<String, Object> serialization = new LinkedHashMap<>(node.getAllProperties());
        Result result = getNodeAttributes(node.getId(), db);
        serialization.put("attributes", serializeAttributes(result, attributesSerializer));
        serialization.put("_id", node.getId());
        serialization.put("id", node.getProperty("id"));
        return serialization;
    }

    private static Result getNodeAttributes(Long nodeId, GraphDatabaseService db) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", nodeId);
        return db.execute("MATCH (n)-->(a:Attribute)-->(v:Value) " +
                "WHERE id(n) = $id RETURN a.title as attribute, v as value", params);
    }

    private static Map<String, Object> serializeAttributes(Result result, AttributesSerializer attributesSerializer) {
        AttributeValueVisitor m = new AttributeValueVisitor();
        try {
            result.accept(m);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return attributesSerializer.serialize(m.getAttributes());
    }

    public static Map<NodePath, List<Interval>> deserializePathIntervalRows(
        List<Map<String, Object>> notIndexedQueryResult, boolean includeLastNode, GraphDatabaseService db) {
    
        Map<NodePath, List<Interval>> result = new HashMap<>();

        for (Map<String, Object> row : notIndexedQueryResult) {
            ArrayList<Map<String, Object>> path = (ArrayList<Map<String, Object>>) row.get("path");
            ArrayList<String> intervals = (ArrayList<String>) row.get("interval");

            NodePath notIndexedNodePath = new NodePath();

            for (int i = 0; i < (includeLastNode ? path.size() : path.size() - 1); i++) {
                notIndexedNodePath.addNodeToPath(db.getNodeById((Long) path.get(i).get("_id")));
            }

            result.put(notIndexedNodePath, intervals.stream().map(interval -> IntervalParser.fromString(interval)).collect(Collectors.toList()));
        }
    
        return result;
    }
}
