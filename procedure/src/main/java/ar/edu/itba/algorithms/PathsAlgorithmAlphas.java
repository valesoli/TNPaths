package ar.edu.itba.algorithms;

import ar.edu.itba.algorithms.strategies.paths.AlphaPathsStrategy;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePair;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPathSensor;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.graph.Graph;
import org.apache.commons.lang3.tuple.Pair;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;


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
       
  
}
