package ar.edu.itba.procedures.graphindex;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Stopwatch;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import ar.edu.itba.algorithms.IndexRetrieveAlgorithm;
import ar.edu.itba.algorithms.strategies.index.ConcatIndexStrategy;
import ar.edu.itba.algorithms.utils.graph.GraphUtils;
import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.algorithms.utils.path.NodePath;
import ar.edu.itba.config.constants.ConfigurationFieldNames;
import ar.edu.itba.records.TemporalPathRecord;

public class RetrievePathsConcat {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="graphindex.retrievePathsConcat")
    @Description("Return the nodes and the intervals that complies with the between condition.")
    public Stream<TemporalPathRecord> retrievePathsFromIndexByConcat (
        @Name("node query") Node startNode,
        @Name("ending node") Node endNode,
        @Name("minimum length") Long minLength,
        @Name("maximum length") Long maxLength,
        @Name("from date") String fromDate,
        @Name("to date") String toDate,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        // TODO validate parameters

        IndexRetrieveAlgorithm algorithm = new IndexRetrieveAlgorithm()
            .setStrategy(new ConcatIndexStrategy(db,
                (String) configuration.get("edgesLabel"),
                startNode,
                endNode,
                fromDate,
                toDate,
                minLength,
                maxLength)
            )
            .setLog(log);

        Stopwatch timer = Stopwatch.createStarted();

        AbstractMap.SimpleImmutableEntry<Map<NodePath, List<Interval>>, Granularity> result = algorithm.run();
        
        timer.stop();
        log.info(String.format("RetrievePathsConcat algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));

        return TemporalPathRecord.getRecordsFromSolutionMap(result.getKey(), db, result.getValue());
    }
    
    @Procedure(value="graphindex.sourceOnlyRetrievePathsConcat")
    @Description("Return the nodes and the intervals that complies with the between condition.")
    public Stream<TemporalPathRecord> sourceOnlyRetrievePathsFromIndexByConcat (
        @Name("node query") Node startNode,
        @Name("minimum length") Long minLength,
        @Name("maximum length") Long maxLength,
        @Name("from date") String fromDate,
        @Name("to date") String toDate,
        @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration
    ) {
        // TODO validate parameters
        IndexRetrieveAlgorithm algorithm = new IndexRetrieveAlgorithm()
            .setStrategy(new ConcatIndexStrategy(db,
                (String) configuration.get("edgesLabel"),
                startNode,
                fromDate,
                toDate,
                minLength,
                maxLength)
            )
            .setLog(log);
        Stopwatch timer = Stopwatch.createStarted();
        AbstractMap.SimpleImmutableEntry<Map<NodePath, List<Interval>>, Granularity> result = algorithm.run();
        timer.stop();
        log.info(String.format("SourceOnlyRetrievePathsConcat algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
            
        return TemporalPathRecord.getRecordsFromSolutionMap(result.getKey(), db, result.getValue());
    }
}
