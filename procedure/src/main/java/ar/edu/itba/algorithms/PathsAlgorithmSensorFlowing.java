package ar.edu.itba.algorithms;

import ar.edu.itba.algorithms.strategies.paths.FlowingSensorPathsStrategy;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPathSensor;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.graph.Graph;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;


import org.apache.commons.lang3.tuple.Pair;

import org.neo4j.logging.Log;

import java.util.List;



public class PathsAlgorithmSensorFlowing extends AbstractAlgorithm<List<IntervalNodePairPathSensor>> {
	protected GraphDatabaseService db;
	private boolean back;

    public PathsAlgorithmSensorFlowing(Graph graph,GraphDatabaseService db) {
        super(graph);
        this.db = db;
        this.back = false;
    }
    
    public PathsAlgorithmSensorFlowing(Graph graph,GraphDatabaseService db, boolean back) {
        super(graph);
        this.db = db;
        this.back = back;
    }
    
    private FlowingSensorPathsStrategy strategy;
    private Node initialNode;
    

    public PathsAlgorithmSensorFlowing setStrategy(FlowingSensorPathsStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public FlowingSensorPathsStrategy getStrategy() {
        return this.strategy;
    }

    public PathsAlgorithmSensorFlowing setInitialNode(Node initialNode) {
        //this.log.info(String.format("Initial node: %s.", initialNode));
        this.initialNode = initialNode;
        return this;
    }
    
    public Node getInitialNode() {
        return this.initialNode;
    }
    
    public PathsAlgorithmSensorFlowing setEndingNode(Node endingNode) {
        //this.log.info(String.format("Ending node: %s.", endingNode));
        this.strategy.setEndingNode(endingNode);
        return this;
    }

    public PathsAlgorithmSensorFlowing setLog(Log log) {
        return (PathsAlgorithmSensorFlowing) super.setLog(log);
    }
    
    boolean isBackwards(){
    	return this.back;
    }
    
    @Override
    public List<IntervalNodePairPathSensor> run() {
    	
    	Node inode = getInitialNode();
    	
    	Long naId =this.strategy.measuresVariable(inode);
    	if ((!this.strategy.isSensor(inode)) || (naId == null)){
    		System.out.println("Must start from a Sensor Node");
    		return null;
    	}
    	
    	javafx.util.Pair<IntervalSet,Long> otherPair;
    	javafx.util.Pair<IntervalSet, Long> tempis;
    	if (isBackwards()){
    		tempis = this.strategy.getValueFirstIntervalB(naId, 
    			this.strategy.getValue(), 
    			this.graph.getBetweenInterval());
    	}
    	else{
    		tempis = this.strategy.getValueIntervals(naId, 
        			this.strategy.getValue(), 
        			this.graph.getBetweenInterval(), 
        			this.graph.getBetweenInterval());
        	}
    	if (tempis.getKey() == null) return null;
    	
    	this.strategy.addToFrontier(
                new IntervalNodePairPathSensor(inode.getId(), tempis.getKey(), true, tempis.getValue(), 1L, 1L)
        );
    	
    	//Initialize with the search interval so far
        while (!this.strategy.isFinished()) {
        	IntervalNodePairPathSensor currentPair = this.strategy.getNext();

            Node nodo = this.db.getNodeById(currentPair.getNode());                    
            List<Pair<List<Interval>, Long>> lista = this.graph.getRelationshipsFromNode(currentPair.getNode());
          	for(Pair<List<Interval>, Long> interval:lista) {
	          	Node otherNode = this.db.getNodeById(interval.getRight());
	          	naId = this.strategy.measuresVariable(otherNode);
	          	
	          	if (this.strategy.isSensor(otherNode) && (naId != null)) {
	          		boolean comp1 = false;
	          		//Only the useful intervals remain
	          		IntervalSet is1 = currentPair.getIntervalSet().inIntersection(this.graph.getBetweenInterval());
	          		for (Interval i1:is1.getIntervals()){
	          			
	          			//Only the useful intervals remain
	          			if (isBackwards()){
	          				otherPair = this.strategy.getValueIntervalsB(naId, currentPair.getCategory(), i1, this.graph.getBetweenInterval() );
	          				if (otherPair.getKey() != null){
			          			IntervalSet is2 = otherPair.getKey().inIntersection(this.graph.getBetweenInterval());
			          			for (Interval i2:is2.getIntervals()){
			          				comp1 = i1.compareNextDeltaBack(i2, this.strategy.getDelta());
			          				if (comp1) {
			          					this.strategy.expandFrontierSensor(i2, currentPair, interval.getRight(),otherPair.getValue());
			          				}         				
			          			}
		          			}
	          			}
		          		else {
		          			otherPair = this.strategy.getValueIntervals(naId, currentPair.getCategory(), i1, this.graph.getBetweenInterval());
		          			
		          			if (otherPair.getKey() != null){
			          			IntervalSet is2 = otherPair.getKey().inIntersection(this.graph.getBetweenInterval());
			          			for (Interval i2:is2.getIntervals()){
			          				comp1 = i1.compareNextDelta(i2, this.strategy.getDelta());
			          				if (comp1) {
			          					this.strategy.expandFrontierSensor(i2, currentPair, interval.getRight(),otherPair.getValue());
			          				}         				
			          			}
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
	   	return searchInterval.intersection(getValueIntervals(attID)); 
	   	
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
