package ar.edu.itba.util.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class Date implements TimeClass<Date>{

    private final LocalDate localDate;

    public final static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .toFormatter();

    public Date(LocalDate localDate) {
        this.localDate = localDate;
    }

    public static Date parse(String value) {
        return new Date(LocalDate.parse(value));
    }

    public static Date fromDateTime(DateTime dateTime) {
        LocalDateTime localDateTime = dateTime.asDateTime();
        return new Date(LocalDate.from(localDateTime));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Date other = (Date) o;
        return localDate.equals(other.localDate);
    }

    @Override
    public int compareTo(Date date) {
        return localDate.compareTo(date.localDate);
    }

    public LocalDate asLocalDate() {
        return localDate;
    }

    public static Date now() {
        return new Date(LocalDate.now());
    }

    public Long toEpochSecond(boolean dayEnd) {
        LocalTime time = LocalTime.of(0, 0);
        // TODO exclusive at late end
        if (dayEnd)
            time = LocalTime.of(23, 59);
        return LocalDateTime.of(this.localDate, time).toEpochSecond(ZoneOffset.UTC);
    }

    public static DateTimeFormatter formatter() {
        return formatter;
    }
}
