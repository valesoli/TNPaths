package ar.edu.itba.graph.impl;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.config.constants.DirectionType;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.*;

import java.util.LinkedList;
import java.util.List;

public class PartialGraph extends InBaseGraph {

    private final String nodeLabel;
    private final String edgeLabel;
    private static final String TITLE = "title";

    public PartialGraph(
            GraphDatabaseService db,
            String nodeLabel,
            String edgeLabel,
            String nodeInterval,
            String edgeInterval,
            DirectionType directionType,
            Interval betweenInterval,
            boolean pruneNotBetween) {
        super(db, nodeInterval, edgeInterval, directionType, betweenInterval, pruneNotBetween);
        this.nodeLabel = nodeLabel;
        this.edgeLabel = edgeLabel;
    }

    private Iterable<Relationship> getRelationships(Node sourceNode) {
        Iterable<Relationship> relationships;
        if (this.edgeLabel != null) {
            RelationshipType edgeLabelType = RelationshipType.withName(this.edgeLabel);
            relationships = sourceNode.getRelationships(direction, edgeLabelType);
        } else {
            relationships = sourceNode.getRelationships(direction);
        }
        return relationships;
    }

    private List<Interval> getIntervalList(Node sourceNode, Relationship relationship, Node targetNode) {
        if (this.nodeLabel != null) {
            if (targetNode.hasProperty(TITLE) && !targetNode.getProperty(TITLE).equals(this.nodeLabel)) {
                return null;
            }
        }
        String[] intervalString = (String[]) relationship.getProperty(this.edgeInterval);
        List<Interval> intervals = IntervalParser.fromStringArrayToIntervals(
                intervalString);
        this.setGranularityFromList(intervals);
        return this.filterByInterval(intervals);
    }

    @Override
    public List<Pair<List<Interval>, Long>> getRelationshipsFromNode(Long source) {
        Node sourceNode = db.getNodeById(source);
        Iterable<Relationship> relationships = getRelationships(sourceNode);

        List<Pair<List<Interval>, Long>> values = new LinkedList<>();
        relationships.forEach(
                relationship -> {
                    Node otherNode = relationship.getOtherNode(sourceNode);
                    List<Interval> intervals = this.getIntervalList(sourceNode, relationship, otherNode);
                    if (intervals != null) {
                        values.add(Pair.of(intervals, relationship.getOtherNodeId(source)));
                    }
                }
        );
        return values;
    }

    @Override
    public List<Interval> getRelationshipsFromNodeToOther(Long source, Long target) {
        Node sourceNode = db.getNodeById(source);
        Iterable<Relationship> relationships = getRelationships(sourceNode);

        List<Interval> values = new LinkedList<>();
        relationships.forEach(
                relationship -> {
                    Node otherNode = relationship.getOtherNode(sourceNode);
                    if (otherNode.getId() != target) {
                        return;
                    }
                    List<Interval> intervals = this.getIntervalList(sourceNode, relationship, otherNode);
                    if (intervals != null) {
                        values.addAll(intervals);
                    }
                }
        );
        return values;
    }

}
