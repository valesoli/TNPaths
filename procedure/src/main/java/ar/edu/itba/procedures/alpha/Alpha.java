package ar.edu.itba.procedures.alpha;

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
import ar.edu.itba.graph.impl.FullStoredGraph;
import ar.edu.itba.records.BooleanRecord;
import ar.edu.itba.records.TemporalPathIntervalListRecordAlpha;
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
public class Alpha {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;
    
    @Procedure(value="alpha.alphaPath")
    @Description("Get all the Sensor Network alpha paths of sensors from a node, where those nodes measures the variable")
    public Stream<TemporalPathIntervalListRecordAlpha> alphaPath(
            @Name("node query") Node node,
            @Name("ending node") Node endingNode,
            @Name("minimum length") Long min,
            @Name("maximum length") Long max,
            @Name(value = "configuration", defaultValue = "{}") Map<String, Object> configuration) 
            {
    	if (min <= 0 || max < min || max < 2) {
            throw new IllegalArgumentException(
                    "The minimum value cannot be 0 nor the maximum value can be lesser than the minimum or 2.");
        }
        
        
        String att = (String) configuration.get("attribute");
        String op = (String) configuration.get("operator");
        
    	if (!op.equals("=")) {
            throw new IllegalArgumentException(
                    "Only = operator for Alpha pahs allowed.");
        }
    	
    	String val = (String) configuration.get("category");
    	
    	Stopwatch timer = Stopwatch.createStarted();
    	log.info("Initializing alpha.alphaPath algorithm.");
        
    	String delta;
        //default delta
        if (configuration.get("delta")==null)
        	delta = "PT8H";
        else 
        	delta = (String) configuration.get("delta");
        ProcedureConfiguration pconf = new ProcedureConfiguration(configuration);
        List<Integer> eList = pconf.getExcludeList();

        Graph graph = new GraphBuilder(db).buildStored(pconf, true);
        PathsAlgorithmAlphas algorithm = new PathsAlgorithmAlphas(graph,db)
                .setStrategy(new AlphaPathsStrategy(db, min, max, att, op, Long.valueOf(val), 
                		Duration.parse(delta), log))
                .setLog(log)
                .setInitialNode(node)
                .setEndingNode(endingNode);
        List<IntervalNodePairPathSensor> result = algorithm.run();
        timer.stop();
        log.info(String.format("alphaPath algorithm finished in %sms", (timer.elapsed().toNanos() / (double) 1000000)));
        log.info(String.format("Nodes expanded %d.", algorithm.getStrategy().getNodesExpanded()));
      
        Stream<TemporalPathIntervalListRecordAlpha> res= TemporalPathIntervalListRecordAlpha.getRecordsFromSolutionAlphaList
        		(
        				result, 
        				db, 
        				graph.getGranularity(), 
        				att, 
        				(configuration.get("direction").equals("outgoing")),
        				eList);

        return res;
    }
    
    
   
}
