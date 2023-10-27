package ar.edu.itba.records.utils;

import org.neo4j.graphdb.Node;

import java.util.List;
import java.util.Map;

public interface AttributesSensorSerializer {
    Map<String, Object> serialize(Map<String, List<Node>> attributes);
    //Map<String, Object> serializeSensor(Map<String, List<Node>> attributes);
}
