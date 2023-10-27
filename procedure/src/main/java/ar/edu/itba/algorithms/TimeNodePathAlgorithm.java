package ar.edu.itba.algorithms;

import ar.edu.itba.algorithms.strategies.path.PathStrategy;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.transformgraph.TimeNode;
import ar.edu.itba.graph.Graph;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;

import java.util.List;

public class TimeNodePathAlgorithm<T> extends AbstractAlgorithm<List<TimeNode<T>>>{

    private PathStrategy<T> strategy;
    private Node initialNode;
    private Node finishNode;

    public TimeNodePathAlgorithm(Graph graph) {
        super(graph);
    }

    public TimeNodePathAlgorithm<T> setStrategy(PathStrategy<T> strategy) {
        this.strategy = strategy;
        return this;
    }

    public TimeNodePathAlgorithm<T> setInitialNode(Node initialNode) {
        this.log.info(String.format("Initial node: %s", initialNode));
        this.initialNode = initialNode;
        return this;
    }

    public TimeNodePathAlgorithm<T> setFinishNode(Node finishNode) {
        this.log.info(String.format("Goal node: %s", finishNode));
        this.finishNode = finishNode;
        return this;
    }

    public TimeNodePathAlgorithm<T> setLog(Log log) {
        return (TimeNodePathAlgorithm<T>) super.setLog(log);
    }

    public List<TimeNode<T>> run() {
        this.strategy.setGoal((node) -> node.getNodeId().equals(this.finishNode.getId()));
        this.strategy.addToFrontier(new TimeNode<>(this.initialNode.getId(), null));
        while (!this.strategy.isFinished()) {
            TimeNode<T> node = this.strategy.getNext();
            if (this.strategy.isNodeExpanded(node.getNodeId())) {
                this.strategy.expandFrontier(node);
                continue;
            }
            for (Pair<List<Interval>, Long> edge:
                    this.graph.getRelationshipsFromNode(node.getNodeId())) {
                this.strategy.expandFrontier(edge.getLeft(), node, edge.getRight());
            }
        }
        return this.strategy.getSolutions();
    }

}
