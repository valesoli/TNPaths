package ar.edu.itba.procedures.cotemporal;

import ar.edu.itba.algorithms.PathsAlgorithm;
import ar.edu.itba.algorithms.PrunablePathsAlgorithm;
import ar.edu.itba.algorithms.strategies.paths.BooleanCompleteIntersectionPathsStrategy;
import ar.edu.itba.algorithms.strategies.paths.CompleteIntersectionPathsStrategy;
import ar.edu.itba.algorithms.strategies.prunablepath.DirectedPrunablePathStrategy;
import ar.edu.itba.algorithms.strategies.prunablepath.DirectionlessPrunablePathStrategy;
import ar.edu.itba.algorithms.strategies.prunablepath.PrunablePathStrategy;
import ar.edu.itba.algorithms.utils.graph.GraphUtils;
import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePair;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.config.ProcedureConfiguration;
import ar.edu.itba.config.constants.ConfigurationFieldNames;
import ar.edu.itba.graph.Graph;
import ar.edu.itba.graph.GraphBuilder;
import ar.edu.itba.records.BooleanRecord;
import ar.edu.itba.records.TemporalPathIntervalListRecord;
import ar.edu.itba.records.TemporalPathRecord;
import com.google.common.base.Stopwatch;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
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

import javax.management.relation.Relation;

@SuppressWarnings("unused")
public class CoTemporalPathsWithPath {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="coexisting.coTemporalPaths")
    @Description("Get all the coexisting paths of length n or less from a node.")
    public Stream<TemporalPathRecord> coTemporalPaths(
            @Name("node query") Node node,
            @Name("ending node") Node endingNode,
            @Name("minimum length") Long min,
            @Name("maximum length") Long max,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {
        if (min <= 0 || max < min) {
            throw new IllegalArgumentException(
                    "The minimum value cannot be 0 nor the maximum value can be lesser than the minimum.");
        }
        // log.info("Initializing coexisting graph algorithm.");
        Stopwatch timer = Stopwatch.createStarted();

        Graph graph = new GraphBuilder(db).buildStored(new ProcedureConfiguration(configuration), false); // Projection es Full
        PathsAlgorithm algorithm = new PathsAlgorithm(graph)
                .setStrategy(new CompleteIntersectionPathsStrategy(min, max, log))
                .setLog(log)
                .setInitialNode(node)
                .setEndingNode(endingNode);
        List<IntervalNodePairPath> result = algorithm.run();
        // Par (node id2, intervalo del nodo anterior intersección intervalo de la relación (n1)-[r:rname]-(n2))
        System.out.println("Resultado común: "+result.toString());
        timer.stop();
        log.info(String.format("CoTemporalPaths algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        // log.info(String.format("Nodes expanded %d.", algorithm.getStrategy().getNodesExpanded()));

        return TemporalPathRecord.getRecordsFromSolutionList(result, db, graph.getGranularity());
    }

    @Procedure(value="coexisting.coTemporalPaths.exists")
    @Description("Returns true if a coexisting path exists between two nodes")
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
        // log.info("Initializing coexisting graph algorithm.");
        Stopwatch timer = Stopwatch.createStarted();

        Graph graph = new GraphBuilder(db).buildStored(new ProcedureConfiguration(configuration), false);
        PathsAlgorithm algorithm = new PathsAlgorithm(graph)
                .setStrategy(new BooleanCompleteIntersectionPathsStrategy(min, max, log))
                .setLog(log)
                .setInitialNode(node)
                .setEndingNode(endingNode);
        List<IntervalNodePairPath> result = algorithm.run();

        timer.stop();
        log.info(String.format("CoTemporalPaths.exists Algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        // log.info(String.format("Nodes expanded %d.", algorithm.getStrategy().getNodesExpanded()));

        return Stream.of(new BooleanRecord(!result.isEmpty()));
    }

    @Procedure(value="coexisting.quickCoTemporalPaths")
    @Description("Get all the coexisting paths of length n or less from a node.")
    public Stream<TemporalPathRecord> quickCoTemporalPaths(
            @Name("node query") Node node,
            @Name("ending node") Node endingNode,
            @Name("minimum length") Long minLength,
            @Name("maximum length") Long maxLength,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {

            // Get between Interval if exists

            Interval betweenInterval = null;
            if (configuration.get(ConfigurationFieldNames.BETWEEN) == null)
                /* TODO throw exception */;
    
            PrunablePathStrategy strategy;
    
            if (configuration.get(ConfigurationFieldNames.DIRECTION) == null)
                strategy = new DirectionlessPrunablePathStrategy(db,
                    (String) configuration.get(ConfigurationFieldNames.EDGE_LABEL),
                    (String) configuration.get(ConfigurationFieldNames.BETWEEN),
                    node,
                    endingNode,
                    minLength,
                    maxLength);
            else
                strategy = new DirectedPrunablePathStrategy(db,
                (String) configuration.get(ConfigurationFieldNames.EDGE_LABEL),
                (String) configuration.get(ConfigurationFieldNames.BETWEEN),
                (String) configuration.get(ConfigurationFieldNames.DIRECTION),
                node,
                endingNode,
                minLength,
                maxLength);
    
            PrunablePathsAlgorithm algorithm = new PrunablePathsAlgorithm()
                .setStrategy(strategy)
                .setLog(log);
    
            Stopwatch timer = Stopwatch.createStarted();

            AbstractMap.SimpleImmutableEntry<List<IntervalNodePairPath>, Granularity> result = algorithm.run();
    
            timer.stop();
            log.info(String.format("QuickCoTemporalPaths algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
            // log.info(String.format("Nodes expanded %d.", algorithm.getStrategy().getNodesExpanded()));

            // TODO check why filtering paths with loops is not possible before
            return TemporalPathRecord.getRecordsFromSolutionList(result.getKey().stream().filter(x -> !x.hasRepeated()).collect(Collectors.toList()), db, result.getValue());
        
        
    }

    @Procedure(value="coexisting.sourceOnlyQuickCoTemporalPaths")
    @Description("Get all the coexisting paths of length n or less from a node.")
    public Stream<TemporalPathRecord> sourceOnlyQuickCoTemporalPaths(
            @Name("node query") Node node,
            @Name("minimum length") Long minLength,
            @Name("maximum length") Long maxLength,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {

        // Get between Interval if exists

        Interval betweenInterval = null;
        if (configuration.get(ConfigurationFieldNames.BETWEEN) == null)
            /* TODO throw exception */;

        PrunablePathStrategy strategy;

        if (configuration.get(ConfigurationFieldNames.DIRECTION) == null)
            strategy = new DirectionlessPrunablePathStrategy(db,
                (String) configuration.get(ConfigurationFieldNames.EDGE_LABEL),
                (String) configuration.get(ConfigurationFieldNames.BETWEEN),
                node,
                minLength,
                maxLength);
        else
            strategy = new DirectedPrunablePathStrategy(db,
            (String) configuration.get(ConfigurationFieldNames.EDGE_LABEL),
            (String) configuration.get(ConfigurationFieldNames.BETWEEN),
            (String) configuration.get(ConfigurationFieldNames.DIRECTION),
            node,
            minLength,
            maxLength);

        PrunablePathsAlgorithm algorithm = new PrunablePathsAlgorithm()
            .setStrategy(strategy)
            .setLog(log);

        Stopwatch timer = Stopwatch.createStarted();

        AbstractMap.SimpleImmutableEntry<List<IntervalNodePairPath>, Granularity> result = algorithm.run();

        timer.stop();
        log.info(String.format("SourceOnlyQuickCoTemporalPaths algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));



        // TODO check why filtering paths with loops is not possible before
        return TemporalPathRecord.getRecordsFromSolutionList(result.getKey().stream().filter(x -> !x.hasRepeated()).collect(Collectors.toList()), db, result.getValue());
    }

}