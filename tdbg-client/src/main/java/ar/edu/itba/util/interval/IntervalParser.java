package ar.edu.itba.util.interval;

import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;

public class IntervalParser {

    public static final String NOW = "now";
    public static final String SEPARATOR = "—";
    public static final String WS_SEPARATOR = " — ";

    public static void main (String [] args) {
        String intervalStr = "2021-03-31 — Now";
        String intervalStr2 = "2014 — 2021-03-31";

        Interval interval = fromString(intervalStr2);
        System.out.println(interval);
    }

    public static Interval fromString(String interval) {
        String fixedString = interval.replace(WS_SEPARATOR, SEPARATOR);
        String[] limits = fixedString.split(SEPARATOR);

        Pair<Long, Granularity> earlyLimit = InstantParser.parse(limits[0]);
        Pair<Long, Granularity> lateLimit;
        boolean isNow = false;
        if (limits[1].toLowerCase().equals(NOW)) {
            lateLimit = InstantParser.nowValue(earlyLimit.getRight());
            isNow = true;
        } else {
            lateLimit = InstantParser.parse(limits[1], true);
        }
        return new Interval(earlyLimit.getLeft(), lateLimit.getLeft(), earlyLimit.getRight().getSmallerGranularity(lateLimit.getRight()), isNow);
    }

    public static Interval fromStringLimits(String earlyLimitString, String lateLimitString) {
        Pair<Long, Granularity> earlyLimit = InstantParser.parse(earlyLimitString);
        Pair<Long, Granularity> lateLimit;
        boolean isNow = false;
        if (lateLimitString.toLowerCase().equals(NOW)) {
            lateLimit = InstantParser.nowValue(earlyLimit.getRight());
            isNow = true;
        } else {
            lateLimit = InstantParser.parse(lateLimitString, true);
        }
        return new Interval(
                earlyLimit.getLeft(), lateLimit.getLeft(),
                earlyLimit.getRight().getSmallerGranularity(lateLimit.getRight()), isNow);
    }

    public static Interval fromStringWithWhitespace(String interval) {
        String fixedString = interval.replace(WS_SEPARATOR, SEPARATOR);
        return IntervalParser.fromString(fixedString);
    }

    public static List<Interval> fromStringArrayToIntervals(String[] intervals) {
        List<Interval> intervalList = new LinkedList<>();
        for (String interval: intervals) {
            intervalList.add(fromString(interval));
        }
        return intervalList;
    }

}
