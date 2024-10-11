package ar.edu.itba.algorithms.strategies.paths;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;

public class CompleteIntersectionAttributePathsStrategy  extends IntervalSetPathStrategy {

	private final String attribute;
	private final String op;
    private final Long value;
    protected GraphDatabaseService db;
    
    public CompleteIntersectionAttributePathsStrategy(GraphDatabaseService db, Long minimumLength, Long maximumLength, String att, String op, Long val, Log log) {
        super(minimumLength, maximumLength, log);
        this.attribute = att;
        this.op = op;
        this.value = val;
        this.db = db;
    }
    
    public String getAttribute(){
    	return this.attribute;
    }
    
    public Long getValue(){
    	return this.value;
    }
    
    public String getOp(){
    	return this.op;
    }
    
    @Override
    public IntervalSet getIntervalSet(IntervalSet node, IntervalSet expandingValue) {
        return expandingValue.intersection(node);
    }
    @Override
    public void expandFrontier(List<Interval> intervalSet, IntervalNodePairPath node, Long otherNodeId ) {
        if (node.isInFullPath(otherNodeId) || node.getNode().equals(otherNodeId) ||
                (this.endingNode != null && otherNodeId.equals(this.endingNode.getId()) && node.getLength() + 1 < this.minimumLength)){
            return;
        }
       
            
        IntervalSet interval = this.getIntervalSet(
                node.getIntervalSet(), new IntervalSet(intervalSet)
        );
        
        if (interval.isEmpty()) { return; }
        IntervalNodePairPath path = new IntervalNodePairPath(otherNodeId, interval, (node.getLength() ));
        path.setPrevious(node);
        Set<Long> previousNodes = node.copyPreviousNodes();
        previousNodes.add(otherNodeId);
        path.setPreviousNodes(previousNodes);
        this.addToFrontier(path);
    }
    
    public void expandFrontierSensor(List<Interval> intervalSet, IntervalNodePairPath node, Long otherNodeId ) {
        if (node.isInFullPath(otherNodeId) || node.getNode().equals(otherNodeId) ||
                (this.endingNode != null && otherNodeId.equals(this.endingNode.getId()) && node.getLength() + 1 < this.minimumLength)){
            return;
        }
       
        
        IntervalSet para_print =new IntervalSet(intervalSet);
        IntervalSet interval = this.getIntervalSet(
                node.getIntervalSet(), new IntervalSet(intervalSet)
        );
        
        if (interval.isEmpty()) { return; }

        IntervalNodePairPath path = new IntervalNodePairPath(otherNodeId, interval, (node.getLength() + 1 ));
        path.setPrevious(node);  
        Set<Long> previousNodes = node.copyPreviousNodes();
        previousNodes.add(otherNodeId);
        path.setPreviousNodes(previousNodes);
        this.addToFrontier(path);
    }
    
	   
   
   
}
