package ar.edu.itba.algorithms.utils.interval;

import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.graphdb.Entity;

import java.util.LinkedList;
import java.util.List;

public class IntervalParser {

    public static final String NOW = "now";
    public static final String SEPARATOR = "—";
    public static final String WS_SEPARATOR = " — ";

    public static void main (String [] args) {
        //String intervalStr = "2022-03-31 — Now";
        //String intervalStr2 = "2022-10-27 — 2022-10-31";
        String intervalStr = "2016—2022";
        String intervalStr2 = "2015—2016";
        Interval interval = fromString(intervalStr);
        Interval interval2 = fromString(intervalStr2);
        
        System.out.println(interval.getStart());
        System.out.println(interval2.getEnd());
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
            lateLimit = InstantParser.parse(limits[1], false);//closed-open intervals. closed-closed: true
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
            lateLimit = InstantParser.parse(lateLimitString, false);//closed-open intervals: false. closed-closed: true
        }
        return new Interval(
                earlyLimit.getLeft(), lateLimit.getLeft(),
                earlyLimit.getRight().getSmallerGranularity(lateLimit.getRight()), isNow);
    }

    public static Interval fromStringWithWhitespace(String interval) {
        String fixedString = interval.replace(WS_SEPARATOR, SEPARATOR);
        return IntervalParser.fromString(fixedString);
    }

    public static List<Interval> entityToIntervals(Entity entity) {
        if (!entity.hasProperty("interval")) {
            // TODO - CHECK IF NEEDED TO REMOVE. FOR SOME REASON ALGORITHM IS CHECKING FOR INTERVAL IN ATTRIBUTE NODES 
            List<Interval> ret = new LinkedList<>();
            ret.add(new Interval(Long.MIN_VALUE, Long.MAX_VALUE, Granularity.DATETIME));
            return ret;
        }
        String[] intervals = (String[]) entity.getProperty("interval");
        List<Interval> intervalList = new LinkedList<>();
        for (String interval : intervals) {
            intervalList.add(IntervalParser.fromString(interval));
        }
        return intervalList;
    }

    public static List<Interval> fromStringArrayToIntervals(String[] intervals) {
        List<Interval> intervalList = new LinkedList<>();
        for (String interval: intervals) {
            intervalList.add(fromString(interval));
        }
        return intervalList;
    }

}
