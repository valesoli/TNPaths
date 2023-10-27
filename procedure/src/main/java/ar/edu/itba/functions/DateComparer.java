package ar.edu.itba.functions;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserFunction;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;

public class DateComparer {
    
    @UserFunction(name="temporal.intersectsInterval")
    @Description("Check if from-to date pair intersects interval string")
    public Boolean intersectsInterval(
        @Name("indexedIntervalString") String searchIntervalString,
        @Name("indexFromDate") String indexFromDate,
        @Name("indexToDate") String indexToDate
    ){
        Interval searchInterval = IntervalParser.fromString(searchIntervalString);
        Interval indexInterval = IntervalParser.fromStringLimits(indexFromDate, indexToDate);

        return Boolean.valueOf(searchInterval.isIntersecting(indexInterval));
        }

    @UserFunction(name="temporal.containsInterval")
    @Description("Check if from-to date pair contains interval string")
    public Boolean containsInterval(
        @Name("indexedIntervalString") String searchIntervalString,
        @Name("indexFromDate") String indexFromDate,
        @Name("indexToDate") String indexToDate
    ){
        Interval searchInterval = IntervalParser.fromString(searchIntervalString);
        Interval indexInterval = IntervalParser.fromStringLimits(indexFromDate, indexToDate);

        return Boolean.valueOf(indexInterval.containsInterval(searchInterval));
        }
    
    
    @UserFunction(name="temporal.intersectsIntervalSplit")
    @Description("Check if from-to date pair intersects interval string")
    public Boolean intersectsIntervalSplit(
        @Name("indexedIntervalFrom") String searchIntervalFrom,
        @Name("indexedIntervalTo") String searchIntervalTo,
        @Name("indexFromDate") String indexFromDate,
        @Name("indexToDate") String indexToDate
    ){
        Interval searchInterval = IntervalParser.fromStringLimits(searchIntervalFrom, searchIntervalTo);
        Interval indexInterval = IntervalParser.fromStringLimits(indexFromDate, indexToDate);

        return Boolean.valueOf(searchInterval.isIntersecting(indexInterval));
        }
}
