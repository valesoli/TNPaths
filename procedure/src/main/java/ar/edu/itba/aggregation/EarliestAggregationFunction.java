package ar.edu.itba.aggregation;

import ar.edu.itba.algorithms.utils.interval.Interval;
import org.neo4j.procedure.*;

@SuppressWarnings("unused")
public class EarliestAggregationFunction {
    public static final String name = "paths.intervals.earliest";

    @UserAggregationFunction(value = name)
    @Description("Returns the path with the earliest path.")
    public CustomAggregator earliest()
    {
        return new CustomAggregator(Interval::compareEarlier);
    }
}
