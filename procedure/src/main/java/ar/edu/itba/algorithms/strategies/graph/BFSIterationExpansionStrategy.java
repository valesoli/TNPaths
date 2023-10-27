package ar.edu.itba.algorithms.strategies.graph;

import ar.edu.itba.algorithms.Condition;
import ar.edu.itba.algorithms.utils.GraphSaver;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePair;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BFSIterationExpansionStrategy implements GraphStrategy<IntervalNodePair, Long> {

    private Queue<IntervalNodePair> frontier = new LinkedList<>();
    Queue<IntervalNodePair> newFrontier = new LinkedList<>();
    private Long iteration = 0L;
    private Condition<Long> condition;
    private GraphSaver graph;

    public BFSIterationExpansionStrategy() {}

    @Override
    public IntervalNodePair getNext() {
        if (this.frontier.isEmpty()) {
            iteration++;
            this.frontier = newFrontier;
            newFrontier = new LinkedList<>();
        }
        return this.frontier.poll();
    }

    @Override
    public void addToFrontier(IntervalNodePair node) {
        this.newFrontier.offer(node);
        this.graph.addNode(node.getNode());
    }

    @Override
    public boolean isFinished() {
        return this.condition.apply(iteration) || (this.frontier.isEmpty() && this.newFrontier.isEmpty());
    }

    @Override
    public void setGoal(Condition<Long> condition) {
        this.condition = condition;
    }

    @Override
    public IntervalNodePair getSolution() {
        throw new IllegalStateException("This method is unavailable.");
    }

    public void setGraphSaver(GraphSaver graph) {
        this.graph = graph;
    }

    @Override
    public void expandFrontier(List<Interval> intervalSet, IntervalNodePair pair, Long otherNodeId) {
        IntervalSet interval = new IntervalSet(intervalSet)
                .intersection(pair.getIntervalSet());
        if (interval.isEmpty()) { return; }
        this.addToFrontier(new IntervalNodePair(otherNodeId, interval));
    }
}
