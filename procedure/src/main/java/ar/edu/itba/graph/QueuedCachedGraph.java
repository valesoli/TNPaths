package ar.edu.itba.graph;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.graph.weight.LightWeightMap;
import ar.edu.itba.graph.weight.WeightingMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class QueuedCachedGraph {

    private final Map<Long, Set<Long>> adjacentNodes = new HashMap<>();
    private final Queue<Long> queue = new LinkedList<>();
    private final static Long MAX_NODE_SIZE = 100L;
    private final Long queueMaxSize;
    private final WeightingMap weightingMap = new LightWeightMap();

    public QueuedCachedGraph() {
        this.queueMaxSize = MAX_NODE_SIZE;
    }

    public QueuedCachedGraph(Long queueMaxSize) {
        this.queueMaxSize = queueMaxSize;
    }

    public List<Pair<List<Interval>, Long>> getRelationshipsFromStructures(Long source) {
        List<Pair<List<Interval>, Long>> relationships = new LinkedList<>();
        this.getAdjacentNodes(source).forEach(
                (target) -> relationships.add(
                        Pair.of(this.weightingMap.weight(source, target), target)
                )
        );
        return relationships;
    }

    public List<Interval> fromSourceToTarget(Long source, Long target) {
        return this.weightingMap.weight(source, target);
    }

    public void removeWeights() {
        if (queue.size() < this.queueMaxSize) {
            return;
        }
        Long source = queue.poll();
        this.adjacentNodes.get(source).forEach(l -> this.weightingMap.delete(source, l));
        this.adjacentNodes.remove(source);
    }

    public void addNode(Long source, Long target) {
        Set<Long> currentAdjacentNodes;
        if (!adjacentNodes.containsKey(source)){
            currentAdjacentNodes = new HashSet<>();
            this.adjacentNodes.put(source, currentAdjacentNodes);
            queue.add(source);
            removeWeights();
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

    public void setEdgesFromSource(Long source, List<Pair<List<Interval>, Long>> list) {
        list.forEach(pair -> {
            this.weightingMap.put(source, pair.getRight(), pair.getLeft());
            this.addNode(source, pair.getRight());
        });
    }

    public void setEdgesFromSourceAndTarget(Long source, Long target, List<Interval> list) {
        this.weightingMap.put(source, target, list);
        this.addNode(source, target);
    }
}
