package ar.edu.itba.algorithms;

import ar.edu.itba.graph.Graph;
import org.neo4j.logging.Log;

public abstract class AbstractAlgorithm<T> implements Algorithm<T> {

    final Graph graph;
    Log log;

    public AbstractAlgorithm(Graph graph) {
        this.graph = graph;
    }

    public AbstractAlgorithm<T> setLog(Log log) {
        this.log = log;
        return this;
    }

}
