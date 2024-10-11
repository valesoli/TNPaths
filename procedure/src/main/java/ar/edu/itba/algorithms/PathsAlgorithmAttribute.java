package ar.edu.itba.algorithms;

import ar.edu.itba.algorithms.strategies.paths.IntervalSetPathStrategy;
import ar.edu.itba.algorithms.strategies.paths.CompleteIntersectionAttributePathsStrategy;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
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


public class PathsAlgorithmAttribute extends AbstractAlgorithm<List<IntervalNodePairPath>> {
	protected GraphDatabaseService db;

    public PathsAlgorithmAttribute(Graph graph,GraphDatabaseService db) {
        super(graph);
        this.db = db;
    }
    private CompleteIntersectionAttributePathsStrategy strategy;
    private Node initialNode;
    

    public PathsAlgorithmAttribute setStrategy(CompleteIntersectionAttributePathsStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public IntervalSetPathStrategy getStrategy() {
        return this.strategy;
    }

    public PathsAlgorithmAttribute setInitialNode(Node initialNode) {
        //this.log.info(String.format("Initial node: %s.", initialNode));
        this.initialNode = initialNode;
        return this;
    }
    
    public Node getInitialNode() {
        return this.initialNode;
    }
    
    public PathsAlgorithmAttribute setEndingNode(Node endingNode) {
        //this.log.info(String.format("Ending node: %s.", endingNode));
        this.strategy.setEndingNode(endingNode);
        return this;
    }

    public PathsAlgorithmAttribute setLog(Log log) {
        return (PathsAlgorithmAttribute) super.setLog(log);
    }

    @Override
    public List<IntervalNodePairPath> run() {
    	
    	Node inode = getInitialNode();
       	if (!inode.getProperty("title").equals("Sensor")){
    		System.out.println("Must start from a Sensor Node");
    		return null;
    	}
    	IntervalSet inte = new IntervalSet(this.graph.getBetweenInterval());
    	Long isSensor = isValidSensor(inode,inte);
    	if (isSensor != null){
    		IntervalSet new_inte = this.IntervalSensor(isSensor,inte);
    		if (new_inte.isEmpty()) return null;
    		else
    			this.strategy.addToFrontier(
    						new IntervalNodePairPath(inode.getId(), new_inte, 1L)
    						);
    		}
    		else
    			return null;
    	//Initialize with the search interval so far         
        while (!this.strategy.isFinished()) {
        
        	IntervalNodePairPath currentPair = this.strategy.getNext();
            Node nodo = this.db.getNodeById(currentPair.getNode());
                      
            List<Pair<List<Interval>, Long>> lista = this.graph.getRelationshipsFromNode(currentPair.getNode());
            
            for(Pair<List<Interval>, Long> interval:lista) {
            	
            	isSensor = isValidSensor(db.getNodeById(interval.getRight()),inte);
            	//Is this a Sensor Node?
            	if (isSensor != null){
            		IntervalSet new_inte = this.IntervalSensor(isSensor,inte);
            		if (!new_inte.isEmpty())
            			this.strategy.expandFrontierSensor(new_inte.getIntervals(), currentPair, interval.getRight());
	            	}
            	else
            		this.strategy.expandFrontier(currentPair.getIntervalSet().getIntervals(), currentPair, interval.getRight());
            }            
            currentPair.setPreviousNodes(null);
        }
        //TODO Remove extra Segments
        return this.strategy.getSolutionPaths();
    }
    
    public Long isValidSensor(Node node, IntervalSet searchIntervalSet){
    		
    		String sss = (String) node.getProperty("title");
    		if (sss.equals("Sensor")) {
    			IntervalSet iset = new IntervalSet(IntervalParser.entityToIntervals(node));
    			IntervalSet s1 = searchIntervalSet.intersection(iset);
    			if (!s1.isEmpty()){
    				Long na = measuresVariable(node, searchIntervalSet);
    				if (na != null)
    					return na;
    			}
    		}
    		return null;
    }
    public IntervalSet IntervalSensor(Long attID, IntervalSet searchInterval){
    	IntervalSet intervalSet = getValueIntervals(attID);
    	if (intervalSet.isEmpty()) return intervalSet;
    	else return searchInterval.intersection(intervalSet); 
	   	
	   	}

    private Long measuresVariable(Node nodo, IntervalSet searchInterval)  {
    	Long retVal = null;
    
    	Iterable<Relationship> n = nodo.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
        for (Relationship n1:n){
        	
        	Node na = this.db.getNodeById(n1.getEndNodeId());
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
	   boolean found = false;
	   Iterable<Relationship> nvs = na.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
   			for (Relationship n2:nvs){
	           	
	           	Node nv = this.db.getNodeById(n2.getEndNodeId());

	           		if (nv.getProperty("category")== this.strategy.getValue()){
	           			found = true;
		           		List<Interval> inter = IntervalParser.fromStringArrayToIntervals((String []) nv.getProperty("interval"));		           		
		           		is = new IntervalSet(inter);
		           		
		           		return is;
		           		//break;
		           	}
	           /*	case ">=":
	           		if ((Long) nv.getProperty("category") >= this.strategy.getValue()){
		           		List<Interval> inter = IntervalParser.fromStringArrayToIntervals((String []) nv.getProperty("interval"));
		           		for (Interval i:inter){
		           			is = is.union(i);
		           	}
	           	}
	           		
	           	}
	           	*/
	           	
               }
   		
   	    if (found) return is;
   	    else return new IntervalSet();
   			
       	}
       
  
}
