package ar.edu.itba.graph;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This abstract class represents the information of a Graph. It is used to hide the source of the information we get.
 * This could be projected from a query we receive, or got from the database service.
 */
public abstract class Graph {

    private final Interval betweenInterval;
    private final boolean pruneNotBetween;
    protected Granularity granularity = null;

    protected Graph(Interval betweenInterval, boolean pruneNotBetween) {
        this.betweenInterval = betweenInterval;
        this.pruneNotBetween = pruneNotBetween;
    }
    public Interval getBetweenInterval(){
    	return this.betweenInterval;
    }
    
    /**
     * Returns all the relationships whose source is the node with the id received. This relationships are stored in a
     * List, and are represented by the Pair objects. This contains in the left side an IntervalSet that
     * represents all the Interval in the edge between the source node and the target node. The right side is the id
     * of the target node.
     *
     * @param source The id of the source node.
     * @return A List of Pair that represents all the relationships that starts in the source node.
     */
    public abstract List<Pair<List<Interval>, Long>> getRelationshipsFromNode(Long source);

    /**
     * Returns the intervals associated with the node.
     *
     * @param nodeId the Id of the Node.
     * @return An array of intervals associated with the node.
     */
    public abstract List<Interval> getIntervalsFromNode(Long nodeId);

    /**
     * Returns the granularity of the intervals in the Graph.
     *
     * @return Returns the granularity of the intervals.
     */
    public abstract Granularity getGranularity();

    /**
     * Returns the intervals from a node to another.
     * @param source The id of the source node.
     * @param target The id of the target node.
     * @return A List of Pair that represents all the relationships that starts in the source node
     */
    public abstract List<Interval> getRelationshipsFromNodeToOther(Long source, Long target);

    protected List<Interval> filterByInterval(List<Interval> intervalSet) {
        if (this.betweenInterval != null) {
            return intervalSet.stream().filter(
                    (i) -> {
                        if (this.pruneNotBetween) {
                            return i.isBetween(this.betweenInterval);
                        } else {
                            return i.isIntersecting(this.betweenInterval);
                        }
                    }
            ).collect(Collectors.toList());
        } else {
            return intervalSet;
        }
    }

    protected void setGranularityFromList(List<Interval> intervals) {
        for (Interval interval: intervals) {
            this.granularity = this.granularity == null ? interval.getGranularity() :
                    this.granularity.getSmallerGranularity(interval.getGranularity());
        }
    }
}
