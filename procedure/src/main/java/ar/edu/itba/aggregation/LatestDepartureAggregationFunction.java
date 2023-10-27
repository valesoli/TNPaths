package ar.edu.itba.aggregation;

import ar.edu.itba.algorithms.utils.interval.Interval;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.UserAggregationFunction;

@SuppressWarnings("unused")
public class LatestDepartureAggregationFunction {
    public static final String name = "paths.intervals.latestDeparture";

    @UserAggregationFunction(value = name)
    @Description("Returns the path with the latest departure path.")
    public CustomAggregator fastest()
    {
        return new CustomAggregator(Interval::compareLatestDeparture);
    }
}
