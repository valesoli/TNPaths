package ar.edu.itba.population;

import ar.edu.itba.util.Utils;

import java.time.LocalDate;
import java.util.*;

public class TimeInterval {

    public static final int TIME_INTERVAL_MIN_YEAR = 1960;

    public static final int TIME_INTERVAL_MAX_YEAR = LocalDate.now().getYear();

    private static final String NOW = "Now";

    private static final String SEPARATOR = "—";

    private final Integer start;

    private final Integer end;

    public TimeInterval() {
        this.start = Utils.randomInteger(TIME_INTERVAL_MIN_YEAR, TIME_INTERVAL_MAX_YEAR);
        this.end = Utils.randomInteger(start + 1, TIME_INTERVAL_MAX_YEAR + 1);
    }

    public TimeInterval(final int end) {
        this.start = Utils.randomInteger(TIME_INTERVAL_MIN_YEAR, end);
        this.end = end;
    }

    public TimeInterval(final int start, final int end) {

        if (start > end) {
            throw new IllegalArgumentException(String.format("Start (%d) must be smaller than end (%d)", start, end));
        }

        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(start).append(SEPARATOR).
                append(end == TIME_INTERVAL_MAX_YEAR ? NOW : end.toString()).toString();
    }

    /**
     * Returns the intersection between two intervals.
     *
     * @param t1 first interval
     * @param t2 second interval
     * @return an interval with the intersection or an empty {@link Optional} if intervals are disjoint.
     */
    public static Optional<TimeInterval> intersection(final TimeInterval t1, final TimeInterval t2) {

        if (t1.endsBefore(t2) || t2.endsBefore(t1)) {
            return Optional.empty();
        }

        int start = Math.max(t1.getStart(), t2.getStart());
        int end = Math.min(t1.getEnd(), t2.getEnd());

        return Optional.of(new TimeInterval(start, end));
    }

    /**
     * Checks if this interval ended before another interval starts.
     *
     * @param other interval
     * @return true if this ended before the other interval starts
     */
    public boolean endsBefore(TimeInterval other) {
        return getEnd() <= other.getStart();
    }

    /**
     * Returns a random interval between the specified origin and the specified bound.
     *
     * @param origin lower limit of interval
     * @param bound  upper bound of interval
     * @return a random interval between the origin (inclusive) and bound (exclusive)
     */
    public static TimeInterval randomInterval(final int origin, int bound) {

        if (origin >= bound) {
            throw new IllegalArgumentException("bound must be greater than origin");
        }

        int start = Utils.randomInteger(origin, bound);
        int end = Utils.randomInteger(start + 1, bound + 1);
        return new TimeInterval(start, end);
    }

    /**
     * Generates a list of random intervals
     * @param amount number of intervals
     * @return time interval list with stats
     * @see TimeIntervalListWithStats
     */
    public static TimeIntervalListWithStats list(int amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("number of intervals must be positive");
        }

        int maxStart = 0;
        int minEnd = TIME_INTERVAL_MAX_YEAR;
        List<TimeInterval> intervals = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            final int MIN_AGE = 20;
            int start = Utils.randomInteger(TIME_INTERVAL_MIN_YEAR, TIME_INTERVAL_MAX_YEAR - MIN_AGE);
            TimeInterval interval = new TimeInterval(start, TimeInterval.TIME_INTERVAL_MAX_YEAR);
            int end = interval.getEnd();

            if (start > maxStart) {
                maxStart = start;
            }

            if (end < minEnd) {
                minEnd = end;
            }

            intervals.add(interval);
        }

        return new TimeIntervalListWithStats(intervals, maxStart, minEnd);
    }

    /**
     * Creates a number of disjoint random intervals within another interval.
     *
     * @param timeInterval      main interval
     * @param maxNumberOfIntervals amount of intervals
     * @return list of intervals
     */
    public static List<TimeInterval> createRandomDisjointIntervals(TimeInterval timeInterval, int maxNumberOfIntervals) {

        if (intervalIsSmallerThanNumberOfIntervals(timeInterval, maxNumberOfIntervals)) {
            return Collections.singletonList(new TimeInterval(timeInterval.getStart(), timeInterval.getEnd()));
        }

        List<TimeInterval> intervals = new ArrayList<>();

        int start = timeInterval.getStart();
        int end = timeInterval.getEnd();

        for (int i = 0; i < maxNumberOfIntervals; i++) {
            TimeInterval interval = TimeInterval.randomInterval(start, end);
            intervals.add(interval);
            start = interval.getEnd() + 1;
            if (interval.getEnd() == timeInterval.getEnd() || start == end) {
                break;
            }
        }

        return intervals;
    }

    /**
     * Creates random consecutive intervals within another interval
     * @param timeInterval base interval
     * @param numberOfIntervals how many intervals
     * @return a list of consecutive random intervals within another one
     */
    public static List<TimeInterval> createRandomConsecutiveIntervals(TimeInterval timeInterval, int numberOfIntervals) {

        List<TimeInterval> intervals = new ArrayList<>();

        int start = timeInterval.getStart();

        if (intervalIsSmallerThanNumberOfIntervals(timeInterval, numberOfIntervals)) {
            return Collections.singletonList(new TimeInterval(timeInterval.getStart(), timeInterval.getEnd()));
        }

        List<Integer> breakPoints = Utils.randomConsecutive(start + 1, timeInterval.getEnd() - 1, numberOfIntervals - 1);

        for (int end : breakPoints) {
            intervals.add(new TimeInterval(start, end));
            start = end + 1;
        }

        //Last interval
        intervals.add(new TimeInterval(start, timeInterval.getEnd()));

        return intervals;
    }

    public static boolean intervalIsSmallerThanNumberOfIntervals(TimeInterval interval, int numberOfIntervals) {
        return interval.getEnd() - interval.getStart() <= numberOfIntervals;
    }

    public TimeInterval getRandomSubInterval() {
        int subIntervalStart = Utils.randomInteger(start, end);
        int subIntervalEnd = Utils.randomInteger(subIntervalStart, end);
        return new TimeInterval(subIntervalStart, subIntervalEnd);
    }
}
