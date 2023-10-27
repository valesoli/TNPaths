package ar.edu.itba.aggregation;

import ar.edu.itba.algorithms.utils.interval.Interval;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.UserAggregationFunction;

@SuppressWarnings("unused")
public class FastestAggregationFunction {
    public static final String name = "paths.intervals.fastest";

    @UserAggregationFunction(value = name)
    @Description("Returns the path with the fastest path.")
    public CustomAggregator fastest()
    {
        return new CustomAggregator(Interval::compareTinier);
    }
}
