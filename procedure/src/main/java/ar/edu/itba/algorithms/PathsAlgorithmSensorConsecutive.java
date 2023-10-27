package ar.edu.itba.algorithms;

import ar.edu.itba.algorithms.strategies.paths.IntervalSetPathSensorStrategy ;
import ar.edu.itba.algorithms.strategies.paths.ConsecutiveSensorPathsStrategy;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPathSensor;
import ar.edu.itba.algorithms.utils.interval.IntervalNodeSensorPair;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.graph.Graph;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;

import org.apache.commons.lang3.tuple.Pair;

import org.neo4j.logging.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;


public class PathsAlgorithmSensorConsecutive extends AbstractAlgorithm<List<IntervalNodePairPathSensor>> {
	protected GraphDatabaseService db;

    public PathsAlgorithmSensorConsecutive(Graph graph,GraphDatabaseService db) {
        super(graph);
        this.db = db;
    }
    private ConsecutiveSensorPathsStrategy strategy;
    private Node initialNode;
    

    public PathsAlgorithmSensorConsecutive setStrategy(ConsecutiveSensorPathsStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public ConsecutiveSensorPathsStrategy getStrategy() {
        return this.strategy;
    }

    public PathsAlgorithmSensorConsecutive setInitialNode(Node initialNode) {
        this.log.info(String.format("Initial node: %s.", initialNode));
        this.initialNode = initialNode;
        return this;
    }
    
    public Node getInitialNode() {
        return this.initialNode;
    }
    
    public PathsAlgorithmSensorConsecutive setEndingNode(Node endingNode) {
        this.log.info(String.format("Ending node: %s.", endingNode));
        this.strategy.setEndingNode(endingNode);
        return this;
    }

    public PathsAlgorithmSensorConsecutive setLog(Log log) {
        return (PathsAlgorithmSensorConsecutive) super.setLog(log);
    }

    @Override
    public List<IntervalNodePairPathSensor> run() {
    	
    	Node inode = getInitialNode();
    	System.out.println(inode.getId());

    	if ((!this.strategy.isSensor(inode)) || (!this.strategy.measuresVariable(inode))){
    		System.out.println("Must start from a Sensor Node");
    		return null;
    	}
    	
    	this.strategy.addToFrontier(
                new IntervalNodePairPathSensor(inode.getId(), this.strategy.getValueIntervalFromSensor(inode), true, 1L, 1L)
        );
    	//Initialize with the search interval so far       
        while (!this.strategy.isFinished()) {
        
        	IntervalNodePairPathSensor currentPair = this.strategy.getNext();
        	System.out.println("currentPair " + currentPair.toString1());
            Node nodo = this.db.getNodeById(currentPair.getNode());
                    
            List<Pair<List<Interval>, Long>> lista = this.graph.getRelationshipsFromNode(currentPair.getNode());
          	for(Pair<List<Interval>, Long> interval:lista) {
          		
          	Node otherNode = this.db.getNodeById(interval.getRight());
          	System.out.println("otherNode " + otherNode.toString() + " Interval " + this.strategy.getValueIntervalFromSensor(otherNode).getIntervals().toString());
            
          	if (this.strategy.isSensor(otherNode) && this.strategy.measuresVariable(otherNode)){
          		boolean comp = false;	
          		for (Interval i1:currentPair.getIntervalSet().getIntervals()){
          			System.out.println("i1 " + i1.toString());
          			for (Interval i2:this.strategy.getValueIntervalFromSensor(otherNode).getIntervals()){
          				comp = i1.compareNextDelta(i2, this.strategy.getDelta());
          				System.out.println("compare " + String.valueOf(comp));
          				if (comp){
          					this.strategy.expandFrontierSensor(i2, currentPair, interval.getRight());
          					
          				}
          				
          			}
          		}
          	}
          	else {
          			this.strategy.expandFrontier(interval.getLeft(), currentPair, interval.getRight());
          		}
                    
            }            
            currentPair.setPreviousNodes(null);
        }
        //TODO Remove extra Segments
        return this.strategy.getSolutionPaths();
    }
    
    public Long isValidSensor(Node node, IntervalSet searchIntervalSet){
    		
    		String sss = (String) node.getProperty("title");
    		if (sss.equals("Sensor")) {
    			System.out.println("El title es un Sensor " + sss);
    			IntervalSet s1 = searchIntervalSet.intersection(new IntervalSet(IntervalParser.entityToIntervals(node)));
    		
    			if (!s1.isEmpty()){
    				Long na = measuresVariable(node, searchIntervalSet);
    				if (na != null)
    					return na;
    			}
    		}
    		return null;
    }
    

    
    public IntervalSet IntervalSensor(Long attID, IntervalSet searchInterval){
	   //IntervalSet searchIntervalSet = new IntervalSet(searchInterval);
	   	return searchInterval.intersection(getValueIntervals(attID)); 
	   	
	   	}

    private Long measuresVariable(Node nodo, IntervalSet searchInterval)  {
    	Long retVal = null;
    
    	Iterable<Relationship> n = nodo.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
        for (Relationship n1:n){
        	
        	Node na = this.db.getNodeById(n1.getEndNodeId());
        	System.out.println("na: " + na.toString());
        	if (na.getProperty("title").equals(this.strategy.getAttribute())){
        		List<Interval> inter = IntervalParser.fromStringArrayToIntervals((String []) na.getProperty("interval"));
        		IntervalSet itemp = new IntervalSet(inter);
        		if (!itemp.intersection(searchInterval).isEmpty())
        			retVal = na.getId();
        	}
         }
        return retVal;
    }
    

    
    
   private IntervalSet getValueIntervals(Long attId) {
	   IntervalSet is = new IntervalSet();
	   Node na = this.db.getNodeById(attId);
	   Iterable<Relationship> nvs = na.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
   			for (Relationship n2:nvs){
	           	
	           	Node nv = this.db.getNodeById(n2.getEndNodeId());
	           	
	           	switch (this.strategy.getOp()){
	           	case "=":
	           		if (nv.getProperty("category")== this.strategy.getValue()){
		           		List<Interval> inter = IntervalParser.fromStringArrayToIntervals((String []) nv.getProperty("interval"));
		           		
		           		is = new IntervalSet(inter);
		           		return is;
		           		//break;
		           	}
	           	case ">=":
	           		if ((Long) nv.getProperty("category") >= this.strategy.getValue()){
		           		List<Interval> inter = IntervalParser.fromStringArrayToIntervals((String []) nv.getProperty("interval"));
		           		for (Interval i:inter){
		           			is = is.union(i);
		           	}
	           	}
	           	}
	           	
	           	
               }
       	return is;	
       	}
       
  
}
