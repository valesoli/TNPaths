package ar.edu.itba.algorithms.strategies.graph;

import ar.edu.itba.algorithms.strategies.Strategy;
import ar.edu.itba.algorithms.utils.GraphSaver;

public interface GraphStrategy<T, K> extends Strategy<T, K> {

    void setGraphSaver(GraphSaver graph);

}
