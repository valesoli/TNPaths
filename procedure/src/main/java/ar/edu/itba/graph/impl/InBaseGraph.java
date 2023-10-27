package ar.edu.itba.graph.impl;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.config.constants.DirectionType;
import ar.edu.itba.graph.Graph;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import org.neo4j.graphdb.*;

import java.util.List;

public abstract class InBaseGraph extends Graph {

    final GraphDatabaseService db;
    private final String nodeInterval;
    final String edgeInterval;
    final Direction direction;

    InBaseGraph(
            GraphDatabaseService db,
            String nodeInterval,
            String edgeInterval,
            DirectionType directionType,
            Interval betweenInterval,
            boolean pruneNotBetween) {
        super(betweenInterval, pruneNotBetween);
        this.db = db;
        this.nodeInterval = nodeInterval;
        this.edgeInterval = edgeInterval;
        this.direction = this.fromDirectionType(directionType);
    }

    private Direction fromDirectionType(DirectionType type) {
        switch (type) {
            case BOTH:
                return Direction.BOTH;
            case OUTGOING:
                return Direction.OUTGOING;
            case INCOMING:
                return Direction.INCOMING;
        }
        throw new IllegalArgumentException("Unknown Direction Type");
    }

    @Override
    public List<Interval> getIntervalsFromNode(Long nodeId) {
        Node node = db.getNodeById(nodeId);
        return IntervalParser.fromStringArrayToIntervals((String []) node.getProperty(this.nodeInterval));
    }

    @Override
    public Granularity getGranularity() {
        return this.granularity;
    }

}
