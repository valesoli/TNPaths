package ar.edu.itba.util.interval;

import java.util.List;

public class IntervalListWithStats {

    private final Long maxStart;
    private final Long minEnd;
    private final List<Interval> intervals;

    public IntervalListWithStats(List<Interval> intervals, Long maxStart, Long minEnd) {
        this.intervals = intervals;
        this.maxStart = maxStart;
        this.minEnd = minEnd;
    }

    public List<Interval> getIntervals() {
        return intervals;
    }

    public Long getMaxStart() {
        return maxStart;
    }

    public Long getMinEnd() {
        return minEnd;
    }
}
