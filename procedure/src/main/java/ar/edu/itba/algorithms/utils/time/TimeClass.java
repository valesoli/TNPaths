package ar.edu.itba.algorithms.utils.time;

public interface TimeClass<T> extends Comparable<T> {

    Long toEpochSecond(boolean intervalEnd);

}
