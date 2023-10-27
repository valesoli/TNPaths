package ar.edu.itba.algorithms;

import ar.edu.itba.algorithms.strategies.paths.IntervalSetPathStrategy;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.graph.Graph;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;

import java.util.List;

public class PathsAlgorithm extends AbstractAlgorithm<List<IntervalNodePairPath>> {

    private IntervalSetPathStrategy strategy;
    private Node initialNode;

    public PathsAlgorithm(Graph graph) {
        super(graph);
    }

    public PathsAlgorithm setStrategy(IntervalSetPathStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public IntervalSetPathStrategy getStrategy() {
        return this.strategy;
    }

    public PathsAlgorithm setInitialNode(Node initialNode) {
        // this.log.info(String.format("Initial node: %s.", initialNode));
        this.initialNode = initialNode;
        return this;
    }

    public PathsAlgorithm setEndingNode(Node endingNode) {
        // this.log.info(String.format("Ending node: %s.", endingNode));
        this.strategy.setEndingNode(endingNode);
        return this;
    }

    public PathsAlgorithm setLog(Log log) {
        return (PathsAlgorithm) super.setLog(log);
    }

    @Override
    public List<IntervalNodePairPath> run() {
        this.strategy.addToFrontier(
                new IntervalNodePairPath(initialNode.getId(), null, 0L)
        );
        while (!this.strategy.isFinished()) {
            IntervalNodePairPath currentPair = this.strategy.getNext();
            System.out.println("Proximo par:" + currentPair.toString());
            // getRelationshipsFromNode returns a list of pairs of
            // (intervals for relationship starting from currentNode, Long id of the node on the other side of relationship)
            this.graph.getRelationshipsFromNode(currentPair.getNode()).forEach(
                    interval -> this.strategy.expandFrontier(interval.getLeft(), currentPair, interval.getRight())
            );
            currentPair.setPreviousNodes(null);
        }
        return this.strategy.getSolutionPaths();
    }
}
