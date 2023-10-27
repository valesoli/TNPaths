package ar.edu.itba.algorithms;

import java.util.AbstractMap;
import java.util.List;
import org.neo4j.logging.Log;

import ar.edu.itba.algorithms.strategies.prunablepath.PrunablePathStrategy;
import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;

public class PrunablePathsAlgorithm implements Algorithm<AbstractMap.SimpleImmutableEntry<List<IntervalNodePairPath>, Granularity>>{
    
    private PrunablePathStrategy strategy;
    private Log log;

    @Override
    public AbstractMap.SimpleImmutableEntry<List<IntervalNodePairPath>, Granularity> run() {
        strategy.calculateAllPaths();
        List<IntervalNodePairPath> solution = strategy.getSolution();
        return new AbstractMap.SimpleImmutableEntry<List<IntervalNodePairPath>, Granularity>(solution, strategy.getFinalGranularity());
    }

    public PrunablePathsAlgorithm setLog(Log log) {
        this.log = log;
        // strategy.setLog(log);
        return this;
    } 

    public PrunablePathsAlgorithm setStrategy(PrunablePathStrategy strategy) {
        this.strategy = strategy;
        return this;
    }
}
