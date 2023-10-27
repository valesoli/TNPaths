package ar.edu.itba.algorithms.strategies.paths;

import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import org.neo4j.logging.Log;

public class CompleteIntersectionPathsStrategy  extends IntervalSetPathStrategy {

    public CompleteIntersectionPathsStrategy(Long minimumLength, Long maximumLength, Log log) {
        super(minimumLength, maximumLength, log);
    }

    @Override
    public IntervalSet getIntervalSet(IntervalSet node, IntervalSet expandingValue) {
        return expandingValue.intersection(node);
    }


    /**
     * Class for debugging purposes
     */

    public static IntervalSet classGetIntervalSet(IntervalSet node, IntervalSet expandingValue) {
        return expandingValue.intersection(node);
    }
}
