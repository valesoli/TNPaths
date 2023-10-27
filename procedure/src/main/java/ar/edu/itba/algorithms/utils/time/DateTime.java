package ar.edu.itba.algorithms.utils.time;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;

public class DateTime implements TimeClass<DateTime> {

    private final static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                                                .appendPattern("yyyy-MM-dd HH:mm")
                                                                .toFormatter();

    private final LocalDateTime dateTime;

    public DateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public LocalDateTime asDateTime() {
        return dateTime;
    }

    @Override
    public int compareTo(DateTime other) {
        return this.dateTime.compareTo(other.dateTime);
    }

    public static DateTime now() {
        return new DateTime(LocalDateTime.now());
    }

    public static DateTime parse(String value) {
        return new DateTime(LocalDateTime.parse(value, formatter()));
    }

    public String toString() {
        return formatter().format(dateTime);
    }

    public static DateTimeFormatter formatter() {
        return formatter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateTime other = (DateTime) o;
        return dateTime.equals(other.dateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime);
    }

    public Long toEpochSecond(boolean intervalEnd) {
        return this.dateTime.toEpochSecond(ZoneOffset.UTC);
    }

    public static DateTime fromEpochSecond(Long epoch) {
        return new DateTime(
                LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC));
    }

    public Date getDate() {
        return Date.fromDateTime(this);
    }

    public YearMonth getYearMonth() {
        return new YearMonth(dateTime.getYear(), dateTime.getMonth().getValue());
    }

    public Year getYear() {
        return new Year(dateTime.getYear());
    }
}
