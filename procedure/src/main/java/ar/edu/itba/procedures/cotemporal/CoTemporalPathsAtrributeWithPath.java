package ar.edu.itba.procedures.cotemporal;

import ar.edu.itba.algorithms.PathsAlgorithm;
import ar.edu.itba.algorithms.PathsAlgorithmAttribute;
import ar.edu.itba.algorithms.strategies.paths.BooleanCompleteIntersectionPathsStrategy;
//import ar.edu.itba.algorithms.strategies.paths.CompleteIntersectionPathsStrategy;
import ar.edu.itba.algorithms.strategies.paths.CompleteIntersectionAttributePathsStrategy;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.config.ProcedureConfiguration;
import ar.edu.itba.config.constants.ConfigurationFieldNames;
import ar.edu.itba.graph.Graph;
import ar.edu.itba.graph.GraphBuilder;
import ar.edu.itba.records.BooleanRecord;
import ar.edu.itba.records.TemporalPathRecord;
import com.google.common.base.Stopwatch;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

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
public class CoTemporalPathsAtrributeWithPath {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="coexisting.coTemporalAttributePaths")
    @Description("Get all the coexisting paths of length n or less from a node.")
    public Stream<TemporalPathRecord> coTemporalAttributePaths(
            @Name("node query") Node node,
            @Name("ending node") Node endingNode,
            @Name("minimum length") Long min,
            @Name("maximum length") Long max,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {
        if (min <= 0 || max < min) {
            throw new IllegalArgumentException(
                    "The minimum value cannot be 0 nor the maximum value can be lesser than the minimum.");
        }
        String att = (String) configuration.get(ConfigurationFieldNames.ATTRIBUTE);
        String op = (String) configuration.get("operator");
        String val = (String) configuration.get("category");
        String delta = (String) configuration.get("delta");
        
        String searchIntervalStr = (String) configuration.get(ConfigurationFieldNames.BETWEEN);
        Interval searchInterval = IntervalParser.fromString(searchIntervalStr);
        
        String relationship = (String) configuration.get(ConfigurationFieldNames.EDGE_LABEL);
        log.info("Initializing coexisting attribute graph algorithm.");

        Stopwatch timer = Stopwatch.createStarted();

        Graph graph = new GraphBuilder(db).buildStored(new ProcedureConfiguration(configuration), false);
        PathsAlgorithmAttribute algorithm = new PathsAlgorithmAttribute(graph,db)
                .setStrategy(new CompleteIntersectionAttributePathsStrategy(db, min, max, att, op, Long.valueOf(val), log))
                .setLog(log)
                .setInitialNode(node)
                .setEndingNode(endingNode);
        List<IntervalNodePairPath> result = algorithm.run();

        timer.stop();
        log.info(String.format("CoTemporalAttributePaths algorithm finished in %s.", timer.elapsed()));
        log.info(String.format("Nodes expanded %d.", algorithm.getStrategy().getNodesExpanded()));
        System.out.println("Resultado: "+result.toString());
        if (result==null)
        	return null;
        else
        	return TemporalPathRecord.getRecordsFromSolutionSensorList(result, db, graph.getGranularity());
    }

    @Procedure(value="coexisting.coTemporalAttributePaths.exists")
    @Description("Returns true if a coexisting path exists between two nodes")
    public Stream<BooleanRecord> coTemporalAttributePathExists(
            @Name("node query") Node node,
            @Name("ending node") Node endingNode,
            @Name("minimum length") Long min,
            @Name("maximum length") Long max,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {
        if (min <= 0 || max < min) {
            throw new IllegalArgumentException(
                    "The minimum value cannot be 0 nor the maximum value can be lesser than the minimum.");
        }
        log.info("Initializing coexisting graph algorithm.");
        Stopwatch timer = Stopwatch.createStarted();

        Graph graph = new GraphBuilder(db).buildStored(new ProcedureConfiguration(configuration), false);
        PathsAlgorithm algorithm = new PathsAlgorithm(graph)
                .setStrategy(new BooleanCompleteIntersectionPathsStrategy(min, max, log))
                .setLog(log)
                .setInitialNode(node)
                .setEndingNode(endingNode);
        List<IntervalNodePairPath> result = algorithm.run();

        timer.stop();
        log.info(String.format("CoTemporalPaths.exists Algorithm finished in %s.", timer.elapsed()));
        log.info(String.format("Nodes expanded %d.", algorithm.getStrategy().getNodesExpanded()));

        return Stream.of(new BooleanRecord(!result.isEmpty()));
    }
}
