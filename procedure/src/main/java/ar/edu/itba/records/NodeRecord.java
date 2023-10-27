package ar.edu.itba.records;

import org.neo4j.graphdb.Node;

public class NodeRecord {

    public final Node node;

    public NodeRecord(Node node) {
        this.node = node;
    }
}
