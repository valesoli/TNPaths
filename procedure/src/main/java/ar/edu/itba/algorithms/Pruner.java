package ar.edu.itba.algorithms;

public interface Pruner<T> {

    boolean prune(T t1, T t2);
}
