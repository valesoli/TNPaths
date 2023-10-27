package ar.edu.itba.algorithms.strategies.path.consecutive;

import ar.edu.itba.algorithms.PayloadTransferee;
import ar.edu.itba.algorithms.strategies.path.PathStrategy;
import ar.edu.itba.algorithms.utils.transformgraph.LongTransformGraph;
import ar.edu.itba.algorithms.utils.transformgraph.TimeNode;
import org.neo4j.logging.Log;

import java.util.*;

public abstract class ConsecutivePathStrategy extends PathStrategy<Long> {

    ConsecutivePathStrategy(
            Long difference,
            Comparator<TimeNode<Long>> comparator,
            PayloadTransferee<Long> transferee, Log log) {
        super(comparator, new LongTransformGraph(
                difference, comparator, transferee, log));
    }

}
