package ar.edu.itba.population;

import java.util.List;

public class TimeIntervalListWithStats {

    private final int maxStart;
    private final int minEnd;
    private final List<TimeInterval> intervals;

    public TimeIntervalListWithStats(List<TimeInterval> intervals, int maxStart, int minEnd) {
        this.intervals = intervals;
        this.maxStart = maxStart;
        this.minEnd = minEnd;
    }

    public List<TimeInterval> getIntervals() {
        return intervals;
    }

    public int getMaxStart() {
        return maxStart;
    }

    public int getMinEnd() {
        return minEnd;
    }
}
