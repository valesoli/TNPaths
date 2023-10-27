package ar.edu.itba.util.time;

public interface TimeClass<T> extends Comparable<T> {

    Long toEpochSecond(boolean intervalEnd);

}
