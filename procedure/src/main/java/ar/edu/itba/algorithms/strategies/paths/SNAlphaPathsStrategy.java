package ar.edu.itba.algorithms.strategies.paths;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;

import java.util.List;
import java.util.Set;

import org.neo4j.logging.Log;

public class SNAlphaPathsStrategy extends IntervalSetPathStrategy {

    public SNAlphaPathsStrategy(Long minimumLength, Long maximumLength, Log log) {
        super(minimumLength, maximumLength, log);
    }

    @Override
    public IntervalSet getIntervalSet(IntervalSet node, IntervalSet expandingValue) {
        return expandingValue;
    }

    @Override
    public void expandFrontier(List<Interval> intervalSet, IntervalNodePairPath node, Long otherNodeId) {
        if (node.isInFullPath(otherNodeId) || node.getNode().equals(otherNodeId) ||
                (this.endingNode != null && otherNodeId.equals(this.endingNode.getId()) && node.getLength() + 1 < this.minimumLength)){
            return;
        }
        IntervalSet interval = this.getIntervalSet(
                node.getIntervalSet(), new IntervalSet(intervalSet)
        );
        if (interval.isEmpty()) { return; }
        IntervalNodePairPath path = new IntervalNodePairPath(otherNodeId, interval, node.getLength() + 1);
        path.setPrevious(node);     
        Set<Long> previousNodes = node.copyPreviousNodes();
        previousNodes.add(otherNodeId);
        path.setPreviousNodes(previousNodes);
        this.addToFrontier(path);
    }
}
