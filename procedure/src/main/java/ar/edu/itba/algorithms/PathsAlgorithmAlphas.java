package ar.edu.itba.algorithms;

import ar.edu.itba.algorithms.strategies.paths.AlphaPathsStrategy;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePair;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPathSensor;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.graph.Graph;
//import javafx.util.Pair;
import org.apache.commons.lang3.tuple.Pair;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;


//import org.apache.commons.lang3.tuple.Pair;

import org.neo4j.logging.Log;

import java.util.List;



public class PathsAlgorithmAlphas extends AbstractAlgorithm<List<IntervalNodePairPathSensor>> {
	protected GraphDatabaseService db;

    public PathsAlgorithmAlphas(Graph graph,GraphDatabaseService db) {
        super(graph);
        this.db = db;
    }
    
    
    private AlphaPathsStrategy strategy;
    private Node initialNode;
    

    public PathsAlgorithmAlphas setStrategy(AlphaPathsStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    public AlphaPathsStrategy getStrategy() {
        return this.strategy;
    }

    public PathsAlgorithmAlphas setInitialNode(Node initialNode) {
        this.log.info(String.format("Initial node: %s.", initialNode));
        this.initialNode = initialNode;
        return this;
    }
    
    public Node getInitialNode() {
        return this.initialNode;
    }
    
    public PathsAlgorithmAlphas setEndingNode(Node endingNode) {
        this.log.info(String.format("Ending node: %s.", endingNode));
        this.strategy.setEndingNode(endingNode);
        return this;
    }

    public PathsAlgorithmAlphas setLog(Log log) {
        return (PathsAlgorithmAlphas) super.setLog(log);
    }
    
    
    @Override
    public List<IntervalNodePairPathSensor> run() {
    	
    	Node inode = getInitialNode();
    	System.out.println(inode.getId());
    	Long naId =this.strategy.measuresVariable(inode);
    	if ((!this.strategy.isSensor(inode)) || (naId == null)){
    		System.out.println("Must start from a Sensor Node");
    		return null;
    	}
    	System.out.println("Va a getValueIntervals");
    	IntervalNodePair otherPair;
    	
    	IntervalNodePair tempis = this.strategy.getValueIntervals(naId, 
        			this.strategy.getValue(), 
        			this.graph.getBetweenInterval(), 
        			this.graph.getBetweenInterval());
        	
    	if (tempis.getIntervalSet() == null) return null;
    	//IntervalSet is = tempis.getKey().inIntersection(this.graph.getBetweenInterval());
    	System.out.println("sigue1");
    	this.strategy.addToFrontier(
                new IntervalNodePairPathSensor(inode.getId(), tempis.getIntervalSet(), true, tempis.getNode(), 1L, 1L)
        );
    	
    	//Initialize with the search interval so far
        while (!this.strategy.isFinished()) {
        	IntervalNodePairPathSensor currentPair = this.strategy.getNext();
        	//System.out.println("currentPair " + currentPair.toString1());                    
            List<Pair<List<Interval>, Long>> lista = this.graph.getRelationshipsFromNode(currentPair.getNode());
          	for(Pair<List<Interval>, Long> interval:lista) {
          
	          	Node otherNode = this.db.getNodeById(interval.getRight());
	          	naId = this.strategy.measuresVariable(otherNode);
	          	System.out.println(naId);
	          	if (this.strategy.isSensor(otherNode) && (naId != null)) {
	          		boolean comp1 = false;
	          		//Only the useful intervals remain
	          		IntervalSet is1 = currentPair.getIntervalSet().inIntersection(this.graph.getBetweenInterval());
	          		System.out.println("currentPair " + currentPair.toString1());
	          		for (Interval i1:is1.getIntervals()){
	          			
	          			//Only the useful intervals remain
	          			otherPair = this.strategy.getValueIntervals(naId, currentPair.getCategory(), i1, this.graph.getBetweenInterval());
		          			
		          		if (otherPair.getIntervalSet() != null){
			          			IntervalSet is2 = otherPair.getIntervalSet().inIntersection(this.graph.getBetweenInterval());
			          			for (Interval i2:is2.getIntervals()){
			          				comp1 = i1.compareDelta(i2, this.strategy.getDelta());
			          				if (comp1) {
			          					this.strategy.expandFrontierSensor(i2, currentPair, interval.getRight(),otherPair.getNode());
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
        	System.out.println("en el mismo");
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
