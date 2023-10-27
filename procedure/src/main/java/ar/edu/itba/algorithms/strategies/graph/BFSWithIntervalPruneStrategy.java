package ar.edu.itba.algorithms.strategies.graph;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePair;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;

import java.util.List;

public class BFSWithIntervalPruneStrategy extends BFSIterationExpansionStrategy {

    private final Interval pruneInterval;

    public BFSWithIntervalPruneStrategy(Interval pruneInterval) {
        this.pruneInterval = pruneInterval;
    }

    @Override
    public void expandFrontier(List<Interval> intervalSet, IntervalNodePair pair, Long otherNodeId) {
        IntervalSet interval = new IntervalSet(intervalSet)
                .intersection(pair.getIntervalSet()).intersection(this.pruneInterval);
        if (interval.isEmpty()) { return; }
        this.addToFrontier(new IntervalNodePair(otherNodeId, interval));
    }
}
