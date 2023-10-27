package ar.edu.itba.procedures.graphindex;

import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Stopwatch;

import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Procedure;

import ar.edu.itba.algorithms.IndexRetrieveAlgorithm;
import ar.edu.itba.algorithms.strategies.index.IndexStrategy;
import ar.edu.itba.algorithms.strategies.index.NodeIndexStrategy;
import ar.edu.itba.algorithms.strategies.paths.CompleteIntersectionPathsStrategy;
import ar.edu.itba.algorithms.utils.EntitySerializer;
import ar.edu.itba.algorithms.utils.graph.GraphUtils;
import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.algorithms.utils.path.NodePath;
import ar.edu.itba.procedures.cotemporal.CoTemporalPathsWithPath;
import ar.edu.itba.records.BooleanRecord;
import ar.edu.itba.records.NodeRecord;
import ar.edu.itba.records.RowRecord;
import ar.edu.itba.records.TemporalPathRecord;

@SuppressWarnings("unused")
public class RetrievePathsWithPaths {
   
    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="graphindex.retrievePaths")
    @Description("Return the nodes and the intervals that complies with the between condition.")
    public Stream<TemporalPathRecord> retrievePathsFromIndex (
        @Name("node query") Node startNode,
        @Name("ending node") Node endNode,
        @Name("minimum length") Long minLength,
        @Name("maximum length") Long maxLength,
        @Name("from date") String fromDate,
        @Name("to date") String toDate,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        if (minLength <= 0 || maxLength < minLength) {
            throw new IllegalArgumentException(
                "The minimum value cannot be 0 nor can the maximum value be lesser than the minimum.");
        }
        
        Stopwatch timer = Stopwatch.createStarted();

        IndexRetrieveAlgorithm algorithm = new IndexRetrieveAlgorithm()
        .setStrategy(new NodeIndexStrategy(db,
            (String) configuration.get("edgesLabel"),
            startNode,
            endNode,
            fromDate,
            toDate,
            minLength,
            maxLength)
        )
        .setLog(log);
    
        AbstractMap.SimpleImmutableEntry<Map<NodePath, List<Interval>>, Granularity> result = algorithm.run();
        timer.stop();
        log.info(String.format("RetrievePaths algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        
        return TemporalPathRecord.getRecordsFromSolutionMap(result.getKey(), db, result.getValue());

    }
}
