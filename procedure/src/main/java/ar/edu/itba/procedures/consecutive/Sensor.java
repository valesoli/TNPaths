package ar.edu.itba.procedures.consecutive;

import ar.edu.itba.algorithms.PathsAlgorithm;
import ar.edu.itba.algorithms.PathsAlgorithmAttribute;
import ar.edu.itba.algorithms.PathsAlgorithmAttributeNode;
import ar.edu.itba.algorithms.PathsAlgorithmSensorConsecutive;
import ar.edu.itba.algorithms.PathsAlgorithmSensorFlowing;
import ar.edu.itba.algorithms.PathsAlgorithmAlphas;
import ar.edu.itba.algorithms.strategies.paths.BooleanNodesIntersectionPathsStrategy;
import ar.edu.itba.algorithms.strategies.paths.ConsecutiveSensorPathsStrategy;
import ar.edu.itba.algorithms.strategies.paths.FlowingSensorPathsStrategy;
import ar.edu.itba.algorithms.strategies.paths.AlphaPathsStrategy;
import ar.edu.itba.algorithms.strategies.paths.NodesIntersectionPathsStrategy;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPathSensor;
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

import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class Sensor {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;
    
 
    @Procedure(value="consecutive.sensorFlowing")
    @Description("Get all the flowing paths of n or less sensors from a node where those nodes measures the variable")
    public Stream<TemporalPathIntervalListRecord> sensorFlowing(
            @Name("node query") Node node,
            @Name("ending node") Node endingNode,
            @Name("minimum length") Long min,
            @Name("maximum length") Long max,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {
    	if (min <= 0 || max < min) {
            throw new IllegalArgumentException(
                    "The minimum value cannot be 0 nor the maximum value can be lesser than the minimum.");
        }
        log.info("Initializing consecutive.sensorFlowing algorithm.");
        Stopwatch timer = Stopwatch.createStarted();
        String att = (String) configuration.get("attribute");
        String op = (String) configuration.get("operator");
        String val = (String) configuration.get("category");
        String delta = (String) configuration.get("delta");
        
        Graph graph = new GraphBuilder(db).buildStored(new ProcedureConfiguration(configuration), false);
        PathsAlgorithmSensorFlowing algorithm = new PathsAlgorithmSensorFlowing(graph,db)
                .setStrategy(new FlowingSensorPathsStrategy(db, min, max, att, op, Long.valueOf(val), Duration.parse(delta), log))
                .setLog(log)
                .setInitialNode(node)
                .setEndingNode(endingNode);
        List<IntervalNodePairPathSensor> result = algorithm.run();

        timer.stop();
              
        log.info(String.format("consecutive.Sensor algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        log.info(String.format("Nodes expanded %d.", algorithm.getStrategy().getNodesExpanded()));

        return TemporalPathIntervalListRecord.getRecordsFromSolutionFlowingSensorList(result, db, graph.getGranularity(),att);
    }

    @Procedure(value="consecutive.sensorFlowingBackwards")
    @Description("Get all the flowing paths of n or less sensors from a node where those nodes measures the variable")
    public Stream<TemporalPathIntervalListRecord> sensorFlowingBackwards(
            @Name("node query") Node node,
            @Name("ending node") Node endingNode,
            @Name("minimum length") Long min,
            @Name("maximum length") Long max,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) {
        if (min <= 0 || max < min) {
            throw new IllegalArgumentException(
                    "The minimum value cannot be 0 nor the maximum value can be lesser than the minimum.");
        }
        log.info("Initializing consecutive.sensorFlowing Backwards algorithm.");
        Stopwatch timer = Stopwatch.createStarted();
        String att = (String) configuration.get("attribute");
        String op = (String) configuration.get("operator");
        String val = (String) configuration.get("category");
        String delta = (String) configuration.get("delta");
        
        Graph graph = new GraphBuilder(db).buildStored(new ProcedureConfiguration(configuration), false);
        PathsAlgorithmSensorFlowing algorithm = new PathsAlgorithmSensorFlowing(graph,db,true)
                .setStrategy(new FlowingSensorPathsStrategy(db, min, max, att, op, Long.valueOf(val), Duration.parse(delta), log))
                .setLog(log)
                .setInitialNode(node)
                .setEndingNode(endingNode);
        List<IntervalNodePairPathSensor> result = algorithm.run();

        timer.stop();
        System.out.println("Sale y va a solutions. Size = " + String.valueOf(result.size()));
        System.out.println("Resultado: "+result.toString());
        for (IntervalNodePairPathSensor l:result){
        	System.out.println(l.toString1());
        }
        
        log.info(String.format("consecutive.Sensor algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        log.info(String.format("Nodes expanded %d.", algorithm.getStrategy().getNodesExpanded()));

        return TemporalPathIntervalListRecord.getRecordsFromSolutionFlowingSensorList(result, db, graph.getGranularity(),att);
    }
  

}
