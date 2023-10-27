package ar.edu.itba.util.interval;

import ar.edu.itba.util.time.*;

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
}
