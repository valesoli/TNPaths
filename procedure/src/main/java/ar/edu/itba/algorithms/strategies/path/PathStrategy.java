package ar.edu.itba.algorithms.strategies.path;

import ar.edu.itba.algorithms.strategies.Strategy;
import ar.edu.itba.algorithms.utils.transformgraph.TimeNode;
import ar.edu.itba.algorithms.utils.transformgraph.TransformGraph;
import ar.edu.itba.algorithms.utils.interval.Interval;

import java.util.Comparator;
import java.util.List;

public abstract class PathStrategy<K> implements Strategy<TimeNode<K>, TimeNode<K>> {

    protected final Comparator<TimeNode<K>> comparator;
    protected final TransformGraph<K> graph;
    protected Long nodesExpanded = 0L;

    public PathStrategy(
            Comparator<TimeNode<K>> comparator, TransformGraph<K> graph) {
        this.comparator = comparator;
        this.graph = graph;
    }

    @Override
    public void expandFrontier(List<Interval> intervalSet, TimeNode<K> node, Long otherNode) {
        this.graph.getNewFrontier(intervalSet, node, otherNode).forEach(
                this::addToFrontier
        );
    }

    public void expandFrontier(TimeNode<K> node) {
        this.graph.getNewFrontier(node).forEach(
                this::addToFrontier
        );
    }

    public abstract List<TimeNode<K>> getSolutions();

    public Long getNodesExpanded() {
        return this.nodesExpanded;
    }

    public boolean isNodeExpanded(Long id) {
        return this.graph.isExpanded(id);
    }
}
