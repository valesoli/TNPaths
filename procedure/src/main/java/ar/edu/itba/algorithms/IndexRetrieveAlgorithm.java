package ar.edu.itba.algorithms;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;

import ar.edu.itba.algorithms.strategies.index.IndexStrategy;
import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.path.NodePath;
import ar.edu.itba.procedures.graphindex.RetrieverUtils;

public class IndexRetrieveAlgorithm implements Algorithm<AbstractMap.SimpleImmutableEntry<Map<NodePath, List<Interval>>, Granularity>>{
    
    private IndexStrategy strategy;
    private Log log;

    @Override
    public AbstractMap.SimpleImmutableEntry<Map<NodePath, List<Interval>>, Granularity> run() {
        // log.info("index retrieve algorithm - subtracting meta intervals");
        strategy.subtractMetaIntervals();
        // log.info("index retrieve algorithm - subtracted Meta Intervals");
        Map<NodePath, List<Interval>> solution = strategy.getSolution();
        // log.info("index retrieve algorithm - got Solution");
        Node endNode = strategy.getEndNode();
        return RetrieverUtils.addNotIndexedIfAny(strategy.getNotIndexed(), strategy.getIndexedIntervals(), solution, (Long) strategy.getStartNode().getProperty("id"), endNode == null ? null : (Long) strategy.getEndNode().getProperty("id"), strategy.getEdgesLabel(), strategy.getMinLength(), strategy.getMaxLength(), strategy.getFinalGranularity(), strategy.getDb(), log);
    }

    public IndexRetrieveAlgorithm setLog(Log log) {
        this.log = log;
        strategy.setLog(log);
        return this;
    } 

    public IndexRetrieveAlgorithm setStrategy(IndexStrategy strategy) {
        this.strategy = strategy;
        return this;
    }
}
