package ar.edu.itba.algorithms.utils.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ar.edu.itba.algorithms.utils.interval.Interval;

public class GraphUtils {
    
    private GraphUtils() {}

    public static List<Interval> filterByInterval(List<Interval> intervalSet, Interval betweenInterval, boolean pruneNotBetween) {
        if (betweenInterval != null) {
            return intervalSet.stream().filter(
                    (i) -> {
                        if (pruneNotBetween) {
                            return i.isBetween(betweenInterval);
                        } else {
                            return i.isIntersecting(betweenInterval);
                        }
                    }
            ).collect(Collectors.toList());
        } else {
            return intervalSet;
        }
    }

    public static boolean hasLoops(ArrayList<Long> nodeList) {
        Set<Long> nodeSet = new HashSet<Long>(nodeList);
        return nodeSet.size() < nodeList.size();
    }
}
