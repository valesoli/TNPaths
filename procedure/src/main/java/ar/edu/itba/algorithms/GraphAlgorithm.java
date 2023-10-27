package ar.edu.itba.algorithms;

import ar.edu.itba.algorithms.strategies.graph.GraphStrategy;
import ar.edu.itba.algorithms.utils.GraphSaver;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePair;
import ar.edu.itba.graph.Graph;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;

public class GraphAlgorithm extends AbstractAlgorithm<GraphSaver> {

    private GraphStrategy<IntervalNodePair, Long> strategy;
    private Node initialNode;
    private Condition<Long> condition;

    public GraphAlgorithm(Graph graph) {
        super(graph);
    }

    public GraphAlgorithm setStrategy(GraphStrategy<IntervalNodePair, Long> strategy) {
        this.strategy = strategy;
        return this;
    }

    public GraphAlgorithm setInitialNode(Node initialNode) {
        this.log.info(String.format("Initial node: %s.", initialNode));
        this.initialNode = initialNode;
        return this;
    }

    public GraphAlgorithm setFinishingCondition(Condition<Long> condition) {
        this.log.info("Setting condition.");
        this.condition = condition;
        return this;
    }

    public GraphAlgorithm setLog(Log log) {
        return (GraphAlgorithm) super.setLog(log);
    }

    @Override
    public GraphSaver run() {
        GraphSaver result = new GraphSaver();
        this.strategy.setGoal(this.condition);
        this.strategy.setGraphSaver(result);
        this.strategy.addToFrontier(
                new IntervalNodePair(initialNode.getId(), null)
        );
        while (!this.strategy.isFinished()) {
            IntervalNodePair currentPair = this.strategy.getNext();
            this.graph.getRelationshipsFromNode(currentPair.getNode()).forEach(
                    interval -> this.strategy.expandFrontier(interval.getLeft(), currentPair, interval.getRight())
            );
        }
        return result;
    }
}
