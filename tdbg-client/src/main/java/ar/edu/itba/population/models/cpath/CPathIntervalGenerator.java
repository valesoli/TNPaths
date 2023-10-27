package ar.edu.itba.population.models.cpath;

import ar.edu.itba.util.interval.*;
import ar.edu.itba.util.Pair;
import ar.edu.itba.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class CPathIntervalGenerator {

    private static final double NOISE_FACTOR = (double) 1/3;

    /**
     * Generates the CPath relationship intervals for a list of nodes
     * @param objectNodeIntervals list of object nodes
     * @param subInterval interval in which the relationships must be included
     * @return the relationship intervals and the CPath interval
     */
    public Pair<List<Interval>, Interval> generate(List<Interval> objectNodeIntervals, Interval subInterval) {
        List<Interval> relationshipIntervals = new ArrayList<>();

        Long start = subInterval.getStart();
        Long end = subInterval.getEnd();
        Long noise = (long) Math.floor(NOISE_FACTOR * (end - start));

        Long maxStart = start;
        Long minEnd = end;
        for (Long i = 0L; i < objectNodeIntervals.size() - 1; i++) {

            Long startDelta = getDelta(noise);
            Long endDelta = getDelta(noise);

            Long newStart = start + startDelta;
            Long newEnd = end - endDelta;

            if (newStart >= newEnd) {
                newStart = start;
                newEnd = end;
            }

            if (newStart > maxStart)
                maxStart = newStart;

            if (newEnd < minEnd)
                minEnd = newEnd;

            Interval relationshipInterval = new Interval(newStart, newEnd, Granularity.DATE);
            relationshipIntervals.add(relationshipInterval);
        }
        return new Pair<>(relationshipIntervals, new Interval(maxStart, minEnd, Granularity.DATE));
    }

    private Long getDelta(Long noise) {
        return Utils.randomLong(0L, noise + 1);
    }

}
