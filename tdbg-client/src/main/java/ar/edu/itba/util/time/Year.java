package ar.edu.itba.util.time;

import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class Year implements TimeClass<Year> {

    private final java.time.Year year;

    public Year(java.time.Year year) {
        this.year = year;
    }

    public Year(int year) {
        this.year = java.time.Year.of(year);
    }

    public Year(Long year) {
        this.year = java.time.Year.of(Math.toIntExact(year));
    }


    public static Year parse(String value) {
        return new Year(java.time.Year.parse(value));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Year other = (Year) o;
        return year.equals(other.year);
    }

    @Override
    public int compareTo(Year date) {
        return year.compareTo(date.year);
    }

    public java.time.Year asYear() {
        return year;
    }

    public static Year now() {
        return new Year(java.time.Year.now());
    }

    public Long toEpochSecond(boolean yearEnd) {
        YearMonth yearMonth = new YearMonth(
                java.time.YearMonth.of(this.year.getValue(), Month.JANUARY));
        // TODO exclusive at late end
        if (yearEnd) {
            yearMonth = new YearMonth(
                    java.time.YearMonth.of(this.year.getValue(), Month.DECEMBER));
        }
        return yearMonth.toEpochSecond(yearEnd);
    }

    public static DateTimeFormatter formatter() {
        return new DateTimeFormatterBuilder()
                .appendPattern("yyyy")
                .toFormatter();
    }
}
