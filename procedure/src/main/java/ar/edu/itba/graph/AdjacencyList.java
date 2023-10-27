package ar.edu.itba.graph;

import java.util.*;

public class AdjacencyList {

    private final Map<Long, Set<Long>> adjacentNodes = new HashMap<>();

    public AdjacencyList() {}

    public void addNode(Long source, Long target) {
        Set<Long> currentAdjacentNodes;
        if (!adjacentNodes.containsKey(source)){
            currentAdjacentNodes = new HashSet<>();
            this.adjacentNodes.put(source, currentAdjacentNodes);
        } else {
            currentAdjacentNodes = this.adjacentNodes.get(source);
        }
        currentAdjacentNodes.add(target);
    }

    public Set<Long> getAdjacentNodes(Long source) {
        return this.adjacentNodes.get(source);
    }

    public boolean containsNode(Long source) {
        return this.adjacentNodes.containsKey(source);
    }

}
