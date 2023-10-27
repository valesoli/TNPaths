package ar.edu.itba.graph.weight;

import ar.edu.itba.algorithms.utils.interval.Interval;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightWeightMap implements WeightingMap{

    private final Map<Pair<Long, Long>, List<Interval>> weights = new HashMap<>();

    public LightWeightMap() {
    }

    @Override
    public List<Interval> weight(long source, long target) {
        return weights.get(Pair.of(source, target));
    }

    @Override
    public void put(long source, long target, List<Interval> value) {
        weights.put(Pair.of(source, target), value);
    }

    @Override
    public void delete(long source, long target) {
        weights.remove(Pair.of(source, target));
    }

}
