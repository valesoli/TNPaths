package ar.edu.itba.population.models.cpath;

import ar.edu.itba.util.interval.Granularity;
import ar.edu.itba.util.interval.Interval;
import ar.edu.itba.util.interval.IntervalListWithStats;
import ar.edu.itba.population.TimeIntervalListWithStats;
import ar.edu.itba.util.Pair;

import java.util.List;

public class CPathGenerator {

    private final CPathIntervalGenerator intervalGenerator;

    public CPathGenerator(CPathIntervalGenerator intervalGenerator) {
        this.intervalGenerator = intervalGenerator;
    }

    public Pair<List<Interval>, Interval> generate(IntervalListWithStats timeIntervalListWithStats) {

        List<Interval> objectNodeIntervals = timeIntervalListWithStats.getIntervals();

        if (objectNodeIntervals.isEmpty()) {
            throw new IllegalArgumentException("objectNodeIntervals list can not be empty");
        }

        Interval intersectionBetweenAll = new Interval(
            timeIntervalListWithStats.getMaxStart(),
            timeIntervalListWithStats.getMinEnd(),
            Granularity.DATE
        );

        Interval subInterval = intersectionBetweenAll.getRandomSubInterval();
        return intervalGenerator.generate(objectNodeIntervals, subInterval);
    }
}
