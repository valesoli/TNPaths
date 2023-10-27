package ar.edu.itba.algorithms;

import ar.edu.itba.algorithms.utils.transformgraph.TimeNode;

@FunctionalInterface
public interface PayloadTransferee<T> {

    void transfer(TimeNode<T> offer, TimeNode<T> receiver);

}
