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
        //this.log.info(String.format("Initial node: %s.", initialNode));
        this.initialNode = initialNode;
        return this;
    }
    
    public Node getInitialNode() {
        return this.initialNode;
    }
    
    public PathsAlgorithmAlphas setEndingNode(Node endingNode) {
        //this.log.info(String.format("Ending node: %s.", endingNode));
        this.strategy.setEndingNode(endingNode);
        return this;
    }

    public PathsAlgorithmAlphas setLog(Log log) {
        return (PathsAlgorithmAlphas) super.setLog(log);
    }
    
    
    @Override
    public List<IntervalNodePairPathSensor> run() {
    	
    	Node inode = getInitialNode();
    	Long naId =this.strategy.measuresVariable(inode);
    	if ((!this.strategy.isSensor(inode)) || (naId == null)){
    		System.out.println("Must start from a Sensor Node");
    		return null;
    	}
    	IntervalSet tempis	= this.strategy.getValueIntervals(naId, this.strategy.getValue(), this.graph.getBetweenInterval());
    	if (tempis == null) return null;
    	this.strategy.addToFrontier(
        		new IntervalNodePairPathSensor(inode.getId(), tempis, true, this.strategy.getValue(), 1L, 1L)
        );
    	
    	
        while (!this.strategy.isFinished()) {
        	IntervalNodePairPathSensor currentPair = this.strategy.getNext();
            List<Pair<List<Interval>, Long>> lista = this.graph.getRelationshipsFromNode(currentPair.getNode());
          	for(Pair<List<Interval>, Long> interval:lista) {
              	Node otherNode = this.db.getNodeById(interval.getRight());
	          	naId = this.strategy.measuresVariable(otherNode);
	          	if (this.strategy.isSensor(otherNode) && (naId != null)) {
	              		IntervalSet otherIntervalSet = this.strategy.getValueIntervals(naId, currentPair.getCategory(), this.graph.getBetweenInterval());
		          		if (otherIntervalSet != null) {
		          			this.strategy.expandFrontierSensor(otherIntervalSet, currentPair, interval.getRight(),currentPair.getCategory());
		          		}
		      	   	}
	          	else 
	          			this.strategy.expandFrontier(interval.getLeft(), currentPair, interval.getRight());
            }            
            currentPair.setPreviousNodes(null);
        }
        //TODO Remove extra Segments
        return this.strategy.getSolutionPaths();
    }
       

}
