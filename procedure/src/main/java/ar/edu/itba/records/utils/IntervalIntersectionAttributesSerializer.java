package ar.edu.itba.records.utils;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import org.neo4j.graphdb.Node;

import java.util.*;

public class IntervalIntersectionAttributesSerializer implements AttributesSerializer {

    private final List<Interval> pathIntervals;

    public IntervalIntersectionAttributesSerializer(List<Interval> pathIntervals) {
        this.pathIntervals = pathIntervals;
    }

    @Override
    public Map<String, Object> serialize(Map<String, List<Node>> attributes) {
        Map<String, Object> serializedAttributes = new LinkedHashMap<>();

        for (String attributeName : attributes.keySet()) {
            List<Node> valueNodes = attributes.get(attributeName);

            List<Map<String, Object>> values = new ArrayList<>();
            for (Node node : valueNodes) {
                Map<String,Object> nodeProperties = node.getAllProperties();

                List<Interval> nodeIntervals = IntervalParser.entityToIntervals(node);
                Optional<Interval> intersection = intersection(nodeIntervals);
                if (intersection.isPresent()) {
                    Interval intersectionInterval = intersection.get();
                    //TODO: should be an array?
                    nodeProperties.put("interval", intersectionInterval.toString());
                } else {
                    continue;
                }
                values.add(nodeProperties);
            }

            if (!values.isEmpty()) {
                serializedAttributes.put(attributeName, values);
            }
        }
        return serializedAttributes;
    }

    private Optional<Interval> intersection(List<Interval> nodeIntervals) {
        for (Interval interval : pathIntervals) {
            for (Interval nodeInterval : nodeIntervals) {
                Optional<Interval> intersection = interval.intersection(nodeInterval);
                if (intersection.isPresent()) {
                    return intersection;
                }
            }
        }
        return Optional.empty();
    }

    public Map<String, Object> serializeSensor(Map<String, List<Node>> attributes) {
        Map<String, Object> serializedAttributes = new LinkedHashMap<>();

        for (String attributeName : attributes.keySet()) {
            List<Node> valueNodes = attributes.get(attributeName);

            List<Map<String, Object>> values = new ArrayList<>();
            for (Node node : valueNodes) {
                Map<String,Object> nodeProperties = node.getAllProperties();

                List<Interval> nodeIntervals = IntervalParser.entityToIntervals(node);
                nodeProperties.put("interval", nodeIntervals.toString());
                values.add(nodeProperties);
            }

            if (!values.isEmpty()) {
                serializedAttributes.put(attributeName, values);
            }
        }
        return serializedAttributes;
    }
}
