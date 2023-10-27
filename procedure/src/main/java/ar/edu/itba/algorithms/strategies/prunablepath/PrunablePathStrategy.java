package ar.edu.itba.algorithms.strategies.prunablepath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.DocFlavor.STRING;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;

import ar.edu.itba.algorithms.strategies.SolutionOnlyStrategy;
import ar.edu.itba.algorithms.strategies.paths.CompleteIntersectionPathsStrategy;
import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.config.constants.ConfigurationFieldNames;

public abstract class PrunablePathStrategy implements SolutionOnlyStrategy<List<IntervalNodePairPath>>{

    protected Log log;

    protected Node startNode;
    protected Node endNode;
    protected String edgesLabel;
    protected Long minLength;
    protected Long maxLength;
    protected Granularity finalGranularity = Granularity.YEAR;
    protected GraphDatabaseService db;

    protected Interval betweenInterval;

    protected Result result;

    public PrunablePathStrategy(GraphDatabaseService db, String edgesLabel, String betweenString, Node startNode,
        Node endNode, Long minLength, Long maxLength) {
        this.db = db;
        this.edgesLabel = edgesLabel;
        this.betweenInterval = IntervalParser.fromString(betweenString);
        this.startNode = startNode;
        this.endNode = endNode;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public PrunablePathStrategy(GraphDatabaseService db, String edgesLabel, String betweenString, Node startNode, Long minLength, Long maxLength) {
        this(db, edgesLabel, betweenString, startNode, null, minLength, maxLength);
    }

    protected abstract String generatePrunableInfoQuery();

    public void calculateAllPaths() {
        // Simple implementation of quick cPath

        finalGranularity = Granularity.YEAR;

        Map<String, Object> prunableInfoQueryParams = new HashMap<>();

        prunableInfoQueryParams.put("startId", startNode.getId());
        if (endNode != null)
            prunableInfoQueryParams.put("endId", endNode.getId());

        this.result =  db.execute(generatePrunableInfoQuery(), prunableInfoQueryParams);
    }

    public Granularity getFinalGranularity() {
        return this.finalGranularity;
    }
}
