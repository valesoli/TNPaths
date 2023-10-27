package ar.edu.itba.procedures.cotemporal;

import ar.edu.itba.algorithms.PathsAlgorithm;
import ar.edu.itba.algorithms.strategies.paths.BooleanNodesIntersectionPathsStrategy;
import ar.edu.itba.algorithms.strategies.paths.NodesIntersectionPathsStrategy;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.config.ProcedureConfiguration;
import ar.edu.itba.graph.Graph;
import ar.edu.itba.graph.GraphBuilder;
import ar.edu.itba.records.BooleanRecord;
import ar.edu.itba.records.TemporalPathIntervalListRecord;
import com.google.common.base.Stopwatch;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class CoTemporalPathsWithNodes {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="coexisting.coTemporalPathsNodes")
    @Description("Get all the coexisting paths of length n or less from a node.")
    public Stream<TemporalPathIntervalListRecord> coTemporalPaths(
            @Name("node query") Node node,
            @Name("ending node") Node endingNode,
            @Name("minimum length") Long min,
            @Name("maximum length") Long max,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {
        if (min <= 0 || max < min) {
            throw new IllegalArgumentException(
                    "The minimum value cannot be 0 nor the maximum value can be lesser than the minimum.");
        }
        log.info("Initializing coTemporalPathsNodes algorithm.");
        Stopwatch timer = Stopwatch.createStarted();

        Graph graph = new GraphBuilder(db).buildStored(new ProcedureConfiguration(configuration), false);
        PathsAlgorithm algorithm = new PathsAlgorithm(graph)
                .setStrategy(new NodesIntersectionPathsStrategy(min, max, log))
                .setLog(log)
                .setInitialNode(node)
                .setEndingNode(endingNode);
        List<IntervalNodePairPath> result = algorithm.run();

        timer.stop();
        log.info(String.format("coTemporalPathsNodes algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        log.info(String.format("Nodes expanded %d.", algorithm.getStrategy().getNodesExpanded()));

        return TemporalPathIntervalListRecord.getRecordsFromSolutionList(result, db, graph.getGranularity());
    }

    @Procedure(value="coexisting.coTemporalPathsNodes.exists")
    @Description("Returns true if a path exists")
    public Stream<BooleanRecord> coTemporalPathExists(
            @Name("node query") Node node,
            @Name("ending node") Node endingNode,
            @Name("minimum length") Long min,
            @Name("maximum length") Long max,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {
        if (min <= 0 || max < min) {
            throw new IllegalArgumentException(
                    "The minimum value cannot be 0 nor the maximum value can be lesser than the minimum.");
        }
        log.info("Initializing coTemporalPathsNodes.exists algorithm.");
        Stopwatch timer = Stopwatch.createStarted();

        Graph graph = new GraphBuilder(db).buildStored(new ProcedureConfiguration(configuration), false);
        PathsAlgorithm algorithm = new PathsAlgorithm(graph)
                .setStrategy(new BooleanNodesIntersectionPathsStrategy(min, max, log))
                .setLog(log)
                .setInitialNode(node)
                .setEndingNode(endingNode);
        List<IntervalNodePairPath> result = algorithm.run();

        timer.stop();
        log.info(String.format("coTemporalPathsNodes algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        log.info(String.format("Nodes expanded %d.", algorithm.getStrategy().getNodesExpanded()));

        return Stream.of(new BooleanRecord(!result.isEmpty()));
    }

}
