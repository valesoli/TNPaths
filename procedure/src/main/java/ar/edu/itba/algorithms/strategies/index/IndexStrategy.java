package ar.edu.itba.algorithms.strategies.index;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;

import ar.edu.itba.algorithms.strategies.SolutionOnlyStrategy;
import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.algorithms.utils.path.NodePath;

public abstract class IndexStrategy implements SolutionOnlyStrategy<Map<NodePath, List<Interval>>>{
    
    protected Log log;

    protected Node startNode;
    protected Node endNode;
    protected String fromDate;
    protected String toDate;
    protected String edgesLabel;
    protected Long minLength;
    protected Long maxLength;
    protected Granularity finalGranularity = Granularity.YEAR;
    protected GraphDatabaseService db;

    // TODO check field initialization;
    Interval betweenInterval;
    IntervalSet notIndexed;
    IntervalSet indexedIntervals;

    public IndexStrategy(GraphDatabaseService db, String edgesLabel, Node startNode, Node endNode, String fromDate, String toDate, Long minLength, Long maxLength) {
        this.db = db;
        this.edgesLabel = edgesLabel;
        this.startNode = startNode;
        this.endNode = endNode;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.betweenInterval = IntervalParser.fromStringLimits(fromDate, toDate);
        this.notIndexed = new IntervalSet(betweenInterval);
        this.indexedIntervals = new IntervalSet();
    }

    public IndexStrategy(GraphDatabaseService db, String edgesLabel, Node startNode, String fromDate, String toDate, Long minLength, Long maxLength) {
        this(db, edgesLabel, startNode, null, fromDate, toDate, minLength, maxLength);
    }

    public Node getStartNode () { return this.startNode; }
    public Node getEndNode () { return this.endNode; }
    public Long getMinLength () { return this.minLength; }
    public Long getMaxLength () { return this.maxLength; }
    public GraphDatabaseService getDb() { return this.db; }
    public IntervalSet getNotIndexed() { return this.notIndexed; }
    public IntervalSet getIndexedIntervals() { return this.indexedIntervals; }
    public String getEdgesLabel() { return this.edgesLabel; }
    
    public void setLog (Log log) {
        this.log = log;
    }

    public Granularity getFinalGranularity() {
        return finalGranularity;
    }

    public abstract void subtractMetaIntervals();

}
