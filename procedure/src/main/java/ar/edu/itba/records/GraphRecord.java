package ar.edu.itba.records;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class GraphRecord {

    public Node node;
    public Relationship edge;

    public GraphRecord(Node node) {
        this.node = node;
    }

    public GraphRecord(Relationship edge) {
        this.edge = edge;
    }
}
