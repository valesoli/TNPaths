package ar.edu.itba.aggregation;

import ar.edu.itba.algorithms.utils.interval.Interval;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.UserAggregationFunction;

@SuppressWarnings("unused")
public class LatestArrivalAggregationFunction {
    public static final String name = "paths.intervals.latestArrival";

    @UserAggregationFunction(value = name)
    @Description("Returns the path with the latest arrival path.")
    public CustomAggregator fastest()
    {
        return new CustomAggregator(Interval::compareLatestArrival);
    }
}
