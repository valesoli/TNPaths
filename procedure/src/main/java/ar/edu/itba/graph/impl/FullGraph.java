package ar.edu.itba.graph.impl;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.config.constants.DirectionType;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.*;

import java.util.LinkedList;
import java.util.List;

public class FullGraph extends InBaseGraph {

    public FullGraph(
            GraphDatabaseService db,
            String nodeInterval,
            String edgeInterval,
            DirectionType directionType,
            Interval betweenInterval,
            boolean pruneNotBetween) {
        super(db, nodeInterval, edgeInterval, directionType, betweenInterval, pruneNotBetween);
    }

    private List<Interval> getIntervals(Relationship relationship) {
        if (!relationship.hasProperty(this.edgeInterval)) { return null; }
        String[] intervalString = (String[]) relationship.getProperty(this.edgeInterval);
        List<Interval> intervals = IntervalParser.fromStringArrayToIntervals(intervalString);
        this.setGranularityFromList(intervals);
        return this.filterByInterval(intervals);
    }

    @Override
    public List<Pair<List<Interval>, Long>> getRelationshipsFromNode(Long source) {
        Node sourceNode = db.getNodeById(source);
        final Iterable<Relationship> relationships = sourceNode.getRelationships(direction);
        List<Pair<List<Interval>, Long>> values = new LinkedList<>();
        relationships.forEach(
                relationship -> {
                    if (!relationship.hasProperty(this.edgeInterval)) { return; }
                    String[] intervalString = (String[]) relationship.getProperty(this.edgeInterval);
//                    if (this.granularity == null)
//                        this.granularity = IntervalParser.getGranularityFromIntervalsString(intervalString);
                    List<Interval> intervals = IntervalParser.fromStringArrayToIntervals(
                            intervalString);
                    intervals = this.filterByInterval(intervals);
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
        final Iterable<Relationship> relationships = sourceNode.getRelationships(direction);
        List<Interval> values = new LinkedList<>();
        relationships.forEach(
                relationship -> {
                    if (relationship.getOtherNodeId(source) != target) { return; }
                    List<Interval> intervals = this.getIntervals(relationship);
                    if (intervals != null) {
                        values.addAll(intervals);
                    }
                }
        );
        return values;
    }
}
