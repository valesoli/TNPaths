package ar.edu.itba.aggregation;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.UserAggregationResult;
import org.neo4j.procedure.UserAggregationUpdate;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CustomAggregator {

    private Interval interval;
    private List<Map<String, Object>> paths;
    private Comparator<Interval> comparator;

    public CustomAggregator(Comparator<Interval> comparator) {
        this.comparator = comparator;
    }

    private void changeList(Interval pathInterval, Map<String, Object> current) {
        this.interval = pathInterval;
        this.paths = new LinkedList<>();
        this.paths.add(current);
    }

    @UserAggregationUpdate
    public void find(@Name("path") Map<String, Object> current) {
        if (current == null) {
            return;
        }
        Interval pathInterval =
                IntervalParser.fromStringWithWhitespace(
                        ((List<String>) current.get("interval")).get(0));
        if (this.interval == null) {
            this.changeList(pathInterval, current);
            return;
        }
        int result = comparator.compare(pathInterval, this.interval);
        if (result < 0) {
            this.changeList(pathInterval, current);
        } else if (result == 0) {
            this.paths.add(current);
        }
    }

    @UserAggregationResult
    public List<Map<String, Object>> result()
    {
        return this.paths;
    }

}
