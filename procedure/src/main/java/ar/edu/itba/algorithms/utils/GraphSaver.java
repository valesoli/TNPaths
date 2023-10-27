package ar.edu.itba.algorithms.utils;

import java.util.HashSet;
import java.util.Set;

public class GraphSaver {

    private final Set<Long> nodes = new HashSet<>();
    private final Set<Long> edges = new HashSet<>();

    public GraphSaver() {}

    public void addNode(Long node) {
        this.nodes.add(node);
    }

    public void addEdge(Long edge) {
        this.edges.add(edge);
    }

    public boolean containsNode(Long node) {
        return this.nodes.contains(node);
    }

    public boolean containsEdge(Long edge) {
        return this.edges.contains(edge);
    }

    public Set<Long> getNodes() {
        return this.nodes;
    }

    public Set<Long> getEdges() {
        return this.edges;
    }
}
