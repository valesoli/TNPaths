package ar.edu.itba.records;

import ar.edu.itba.algorithms.utils.interval.Interval;
import org.neo4j.graphdb.Node;

public class NodeIntervalRecord {
    public final Node node;
    public final String initialTime;
    public final String endingTime;

    public NodeIntervalRecord(Node node, Interval interval) {
        this.node = node;
        this.initialTime = interval.getStart().toString();
        this.endingTime = interval.getEnd().toString();
    }
}
