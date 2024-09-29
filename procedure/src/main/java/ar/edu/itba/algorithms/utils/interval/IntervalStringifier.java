package ar.edu.itba.algorithms.utils.interval;

import ar.edu.itba.algorithms.utils.time.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IntervalStringifier {

    public static String intervalToString(Interval interval) {
        LocalDateTime start = DateTime.fromEpochSecond(interval.getStart()).asDateTime();
        LocalDateTime end = DateTime.fromEpochSecond(interval.getEnd()).asDateTime();
        DateTimeFormatter formatter;
        switch (interval.getGranularity()) {
            case DATETIME:
                formatter = DateTime.formatter();
                break;
            case DATE:
                formatter = Date.formatter();
                break;
            case YEAR_MONTH:
                formatter = YearMonth.formatter();
                break;
            case YEAR:
                formatter = Year.formatter();
                break;
            default:
                throw new IllegalArgumentException("Could not parse granularity.");
        }
        String endString;
        if (interval.isNow()) endString = "Now";
        else endString = formatter.format(end);
        return String.format("%s — %s", formatter.format(start), endString);
    }
    public static String getStart(String interval) {
    	Integer hyph = interval.indexOf(" — ");
    	return interval.substring(0,hyph);
    }
    public static String getEnd(String interval) {
    	Integer hyph = interval.indexOf(" — ");
    	return interval.substring(hyph+3, interval.length());
    }
    
    public static void main(String[] args) {
    	System.out.println(getStart("2019 — 2022").length());
    	System.out.println(getEnd("2019 — 2022"));
    }
}
