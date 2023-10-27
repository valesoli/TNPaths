package ar.edu.itba.graph.weight;

import ar.edu.itba.algorithms.utils.interval.Interval;

import java.util.List;

public interface WeightingMap {

    List<Interval> weight(long source, long target);

    void put(long source, long target, List<Interval> value);

    void delete(long source, long target);
}
