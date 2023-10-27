package ar.edu.itba.algorithms.strategies.paths;

import ar.edu.itba.algorithms.utils.interval.Interval;
//import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPathSensor;
import ar.edu.itba.algorithms.utils.interval.IntervalNodeSensorPair;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.logging.Log;

public class ConsecutiveSensorPathsStrategy extends IntervalSetPathSensorStrategy {
	private final String attribute;
	private final String op;
    private final Long value;
    private final Duration delta;
    protected GraphDatabaseService db;
    protected final List<IntervalNodePairPathSensor> sensorSolutions = new LinkedList<>();
    Queue<IntervalNodePairPathSensor> newSensorFrontier = new LinkedList<>();
    
    public ConsecutiveSensorPathsStrategy(GraphDatabaseService db, Long minimumLength, Long maximumLength, 
    		String att, String op, Long val, Duration delta, Log log) {
        super(minimumLength, maximumLength, log);
        this.attribute = att;
        this.op = op;
        this.value = val;
        this.delta = delta;
        this.db = db;
    }

    
    public IntervalSet getIntervalSensorSet(IntervalSet node, IntervalSet expandingValue) {
        return expandingValue.intersectAndReturnThisIntervals(node);
    }
    
    @Override
    public IntervalSet getIntervalSet(IntervalSet node, IntervalSet expandingValue) {
        return expandingValue.intersection(node);
    }
    
    public String getAttribute(){
    	return this.attribute;
    }
    
    public Long getValue(){
    	return this.value;
    }
    
    public Duration getDelta(){
    	return this.delta;
    } 
    public String getOp(){
    	return this.op;
    }
    
    public void addToFrontierSensor(IntervalNodePairPathSensor node) {
        this.nodesExpanded += 1;
        if (node.getLength() >= minimumLength) {
            if (endingNode == null || node.getNode().equals(endingNode.getId())) {
                this.solutions.add(node);
                this.sensorSolutions.add(node);
            }
        }
        if (node.getLength() < maximumLength) {
            this.newFrontier.add(node);
            this.newSensorFrontier.add(node);
        }
    }

    @Override
    public void expandFrontier(List<Interval> intervalSet, IntervalNodePairPathSensor node, Long otherNodeId ) {
        if (node.isInFullPath(otherNodeId) || node.getNode().equals(otherNodeId) ||
                (this.endingNode != null && otherNodeId.equals(this.endingNode.getId()) && node.getLength() + 1 < this.minimumLength)){
            return;
        }
       
        System.out.println("Estoy expandiendo el nodo común "+node.toString() +" que va al nodo " + otherNodeId.toString() );
        
        IntervalNodePairPathSensor path;
    	path = new IntervalNodePairPathSensor(otherNodeId, node.getIntervalSet(), false, node.getLength(),(node.getTotalLength() + 1 ) );
    	
        path.setPrevious(node);
        Set<Long> previousNodes = node.copyPreviousNodes();
        previousNodes.add(otherNodeId);
        path.setPreviousNodes(previousNodes);
        this.addToFrontier(path);
    }
    
    public void expandFrontierSensor(Interval intervalS, IntervalNodePairPathSensor node, Long otherNodeId ) {
        if (node.isInFullPath(otherNodeId) || node.getNode().equals(otherNodeId) ||
                (this.endingNode != null && otherNodeId.equals(this.endingNode.getId()) && node.getLength() + 1 < this.minimumLength)){
            return;
        }
       
        System.out.println("Estoy expandiendo el sensor: "+node.toString());
         	      
    	IntervalNodePairPathSensor path;
    	
        path = new IntervalNodePairPathSensor(otherNodeId, new IntervalSet(intervalS), true, (node.getLength() + 1 ), (node.getTotalLength() + 1 )); 
        
        path.setPrevious(node);
        Set<Long> previousNodes = node.copyPreviousNodes();
        previousNodes.add(otherNodeId);
        path.setPreviousNodes(previousNodes);
        this.addToFrontier(path);
        }
    
    public boolean isSensor(Node node){
    	return node.getProperty("title").equals("Sensor");
    }
    
    private Long getAttributeId(Node node){
		
    	Iterable<Relationship> n = node.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
        for (Relationship n1:n){
        	Node na = this.db.getNodeById(n1.getEndNodeId());
        	if (na.getProperty("title").equals(this.getAttribute())){
        			return na.getId();
        	}
         }
        return null;
    }


public IntervalSet IntervalSensor(Long attID, IntervalSet searchInterval){
   //IntervalSet searchIntervalSet = new IntervalSet(searchInterval);
   	return searchInterval.intersection(getValueIntervals(attID)); 
   	
   	}
public boolean measuresVariable(Node nodo)  {
	
	Iterable<Relationship> n = nodo.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
    for (Relationship n1:n){
    	
    	Node na = this.db.getNodeById(n1.getEndNodeId());
    	System.out.println("na: " + na.toString());
    	if (na.getProperty("title").equals(this.getAttribute())){
    		return true;
    	}
     }
    return false;
}
private Long measuresVariable(Node nodo, IntervalSet searchInterval)  {
	Long retVal = null;

	Iterable<Relationship> n = nodo.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
    for (Relationship n1:n){
    	
    	Node na = this.db.getNodeById(n1.getEndNodeId());
    	System.out.println("na: " + na.toString());
    	if (na.getProperty("title").equals(this.getAttribute())){
    		List<Interval> inter = IntervalParser.fromStringArrayToIntervals((String []) na.getProperty("interval"));
    		IntervalSet itemp = new IntervalSet(inter);
    		if (!itemp.intersection(searchInterval).isEmpty())
    			retVal = na.getId();
    	}
     }
    return retVal;
}
private IntervalSet getSensorsIntersection(IntervalNodeSensorPair lastSensor, Node otherNode){
	IntervalSet valInterval = getValueIntervals(getAttributeId(otherNode));
	return valInterval.intersectAndReturnThisIntervals(lastSensor.getIntervalSet());
	
}


public IntervalSet getValueIntervalFromSensor(Node node){
	return getValueIntervals(getAttributeId(node));
	
}
private IntervalSet getValueIntervals(Long attId) {
   IntervalSet is = new IntervalSet();
   Node na = this.db.getNodeById(attId);
   Iterable<Relationship> nvs = na.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
			for (Relationship n2:nvs){
           	
           	Node nv = this.db.getNodeById(n2.getEndNodeId());
           	
           	switch (this.getOp()){
           	case "=":
           		if (nv.getProperty("category")== this.getValue()){
	           		List<Interval> inter = IntervalParser.fromStringArrayToIntervals((String []) nv.getProperty("interval"));
	           		
	           		is = new IntervalSet(inter);
	           		return is;
	           		//break;
	           	}
           	case ">=":
           		if ((Long) nv.getProperty("category") >= this.getValue()){
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
