package ar.edu.itba.algorithms.utils.time;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class YearMonth implements TimeClass<YearMonth> {

    private final java.time.YearMonth yearMonth;
    private final static DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                                                            .appendPattern("yyyy-MM")
                                                            .toFormatter();

    public YearMonth(java.time.YearMonth yearMonth) {
        this.yearMonth = yearMonth;
    }

    public YearMonth(int year, int month) {
        this.yearMonth = java.time.YearMonth.of(year, month);
    }

    public static YearMonth parse(String value) {
        return new YearMonth(java.time.YearMonth.parse(value, formatter()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YearMonth other = (YearMonth) o;
        return yearMonth.equals(other.yearMonth);
    }

    @Override
    public int compareTo(YearMonth date) {
        return yearMonth.compareTo(date.yearMonth);
    }

    public java.time.YearMonth asYearMonth() {
        return yearMonth;
    }

    public static YearMonth now() {
        return new YearMonth(java.time.YearMonth.now());
    }

    public Long toEpochSecond(boolean monthEnd) {
        LocalTime time = LocalTime.of(0, 0);
        int day = 1;
        if (monthEnd) {
            // TODO intervals were supposed to be exclusive at late end
            time = LocalTime.of(23, 59, 59);
            day = this.yearMonth.lengthOfMonth();
            // time = LocalTime.of(00, 00);
            // day = 1;
        }
        return LocalDateTime.of(
                this.yearMonth.getYear(),
                this.yearMonth.getMonth(),
                day,
                time.getHour(),
                time.getMinute()).toEpochSecond(ZoneOffset.UTC);
    }

    public static DateTimeFormatter formatter() {
        return formatter;
    }
}
