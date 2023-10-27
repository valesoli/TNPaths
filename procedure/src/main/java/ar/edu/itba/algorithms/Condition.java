package ar.edu.itba.algorithms;

@FunctionalInterface
public interface Condition<T> {

    boolean apply(T t);
}
