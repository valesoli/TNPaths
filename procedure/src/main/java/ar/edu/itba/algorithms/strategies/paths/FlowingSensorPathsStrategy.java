package ar.edu.itba.algorithms.strategies.paths;

import ar.edu.itba.algorithms.utils.interval.Interval;
//import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPathSensor;
import ar.edu.itba.algorithms.utils.interval.IntervalNodeSensorPair;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import javafx.util.Pair;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.logging.Log;

public class FlowingSensorPathsStrategy extends IntervalSetPathSensorStrategy {
	private final String attribute;
	private final String op;
    private final Long value;
    private final Duration delta;
    protected GraphDatabaseService db;
    protected final List<IntervalNodePairPathSensor> sensorSolutions = new LinkedList<>();
    Queue<IntervalNodePairPathSensor> newSensorFrontier = new LinkedList<>();
    
    public FlowingSensorPathsStrategy(GraphDatabaseService db, Long minimumLength, Long maximumLength, 
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
    public void expandFrontier(List<Interval> intervalSet, IntervalNodePairPathSensor node, Long otherNodeId) {
        if (node.isInFullPath(otherNodeId) || node.getNode().equals(otherNodeId) ||
                (this.endingNode != null && otherNodeId.equals(this.endingNode.getId()) && node.getLength() + 1 < this.minimumLength)){
            return;
        }
       
        IntervalNodePairPathSensor path;
    	path = new IntervalNodePairPathSensor(otherNodeId, node.getIntervalSet(), false, node.getCategory(),node.getLength(),(node.getTotalLength() + 1 ), false );
        path.setPrevious(node);
        Set<Long> previousNodes = node.copyPreviousNodes();
        previousNodes.add(otherNodeId);
        path.setPreviousNodes(previousNodes);
        this.addToFrontier(path);
    }
    
    public void expandFrontierSensor(Interval intervalS, IntervalNodePairPathSensor node, Long otherNodeId, Long newCategory  ) {
        if (node.isInFullPath(otherNodeId) || node.getNode().equals(otherNodeId) ||
                (this.endingNode != null && otherNodeId.equals(this.endingNode.getId()) && node.getLength() + 1 < this.minimumLength)){
            return;
        }
       
    	IntervalNodePairPathSensor path;
    	
        path = new IntervalNodePairPathSensor(otherNodeId, new IntervalSet(intervalS), true, newCategory, (node.getLength() + 1 ), (node.getTotalLength() + 1 )); 
        
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


/*public IntervalSet IntervalSensor(Long attID, IntervalSet searchInterval){
   //IntervalSet searchIntervalSet = new IntervalSet(searchInterval);
   	return searchInterval.intersection(getValueIntervals(attID, 0L).getKey() ); 
   	
   	}
*/
public Long measuresVariable(Node nodo)  {
	
	Iterable<Relationship> n = nodo.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
    for (Relationship n1:n){
    	Node na = this.db.getNodeById(n1.getEndNodeId());
    	if (na.getProperty("title").equals(this.getAttribute())){
    		return na.getId();
    	}
     }
    return null;
}
public Long measuresVariable(Node nodo, IntervalSet searchInterval)  {
	Long retVal = null;

	Iterable<Relationship> n = nodo.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
    for (Relationship n1:n){
    	
    	Node na = this.db.getNodeById(n1.getEndNodeId());
    	if (na.getProperty("title").equals(this.getAttribute())){
    		List<Interval> inter = IntervalParser.fromStringArrayToIntervals((String []) na.getProperty("interval"));
    		IntervalSet itemp = new IntervalSet(inter);
    		
    		if (!itemp.intersection(searchInterval).isEmpty())
    			retVal = na.getId();
    	}
     }
    return retVal;
}

public Pair<IntervalSet,Long> getValueIntervals(Long attId, Long prev, Interval interval, Interval searchInterval  ) {
	Node na = this.db.getNodeById(attId);
	Long cate = 0L;
	Pair<IntervalSet,Long> closest = new Pair<>(null,prev);
	Iterable<Relationship> nvs = na.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
	for (Relationship n2:nvs){       	
           	Node nv = this.db.getNodeById(n2.getEndNodeId());
           	cate = (Long) nv.getProperty("category");
           	switch (this.getOp().toLowerCase()){
           	case "up":          		    		
           		if (cate >= prev)
           			closest = getClosestPairForward(nv,closest,interval,searchInterval,cate);
           		break;
        	case "=":          		    		
           		if (cate == this.getValue())
           			closest = getClosestPairForward(nv,closest,interval,searchInterval,cate);
           		break;
        	case "sup":
        		if (cate > prev)
           			closest = getClosestPairForward(nv,closest,interval,searchInterval,cate);
           		break;
        	case "do":
        		if (cate <= prev)
           			closest = getClosestPairForward(nv,closest,interval,searchInterval,cate);
           		break;
        	case "sdo":
        		if (cate < prev)
           			closest = getClosestPairForward(nv,closest,interval,searchInterval,cate);
           		break;
           		
           	}       	
           }
	return closest;	
   	}

public Pair<IntervalSet,Long> getValueFirstIntervalB(Long attId, Long value, Interval searchInterval ) {
	Node na = this.db.getNodeById(attId);
	Long cate = 0L;
	Pair<IntervalSet,Long> closest = new Pair<>(null,null);
	Iterable<Relationship> nvs = na.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
	for (Relationship n2:nvs){       	
           	Node nv = this.db.getNodeById(n2.getEndNodeId());
           	cate = (Long) nv.getProperty("category");
           	switch (this.getOp().toLowerCase()){
           	case "up":          		    		
           		if (cate >= value)
           			closest = getClosestPairFirstBackwards(nv,closest,searchInterval,cate);
           		break;
        	case "=":          		    		
           		if (cate == this.getValue())
           			closest = getClosestPairFirstBackwards(nv,closest,searchInterval,cate);
           		break;
        	case "sup":
        		if (cate > this.getValue())
           			closest = getClosestPairFirstBackwards(nv,closest,searchInterval,cate);
           		break;
        	case "do":
        		if (cate <= this.getValue())
           			closest = getClosestPairFirstBackwards(nv,closest,searchInterval,cate);
           		break;
        	case "sdo":
        		if (cate < this.getValue())
           			closest = getClosestPairFirstBackwards(nv,closest,searchInterval,cate);
           		break;
           		
           	}       	
           }
		    return closest;	
	   	}


private Pair<IntervalSet,Long> getClosestPairForward(Node valueNode, Pair<IntervalSet,Long> closest, Interval interval, Interval searchInterval, Long cate  )
{
	boolean found = false;
	Integer i = 0;
	List<Interval> inter = IntervalParser.fromStringArrayToIntervals((String []) valueNode.getProperty("interval"));
	while (i < inter.size() && !found){		
		if (inter.get(i).getStart() > interval.getStart() && inter.get(i).isIntersecting(searchInterval)){
			found=true;
			if (closest.getKey()==null || inter.get(i).getStart() < closest.getKey().getLast().getStart())
				{closest = new Pair<>(new IntervalSet(inter.get(i)),cate);
				}
			}
		i++;
		}
	return closest;
}

private Pair<IntervalSet,Long> getClosestPairBackwards(Node valueNode, Pair<IntervalSet,Long> closest, Interval interval, Interval searchInterval, Long cate  )
{
	boolean found = false;
	List<Interval> inter = IntervalParser.fromStringArrayToIntervals((String []) valueNode.getProperty("interval"));
	Integer i = inter.size()-1;
	while (i > -1 && !found){		
		if (inter.get(i).getStart() < interval.getStart() && inter.get(i).isIntersecting(searchInterval)){
			found=true;
			if (closest.getKey()==null || inter.get(i).getStart() > closest.getKey().getLast().getStart())
				{closest = new Pair<>(new IntervalSet(inter.get(i)),cate);
				}				
			}
		i--;
		}
	return closest;
}

private Pair<IntervalSet,Long> getClosestPairFirstBackwards(Node valueNode, Pair<IntervalSet,Long> closest, Interval searchInterval, Long cate  )
{
	boolean found = false;
	List<Interval> inter = IntervalParser.fromStringArrayToIntervals((String []) valueNode.getProperty("interval"));
	Integer i = inter.size()-1;
	while (i > -1 && !found){		
		if (inter.get(i).getStart() < searchInterval.getEnd()){
			found=true;
			if (closest.getKey()==null || inter.get(i).getStart() > closest.getKey().getLast().getStart())
				{closest = new Pair<>(new IntervalSet(inter.get(i)),cate);
				}				
			}
		i--;
		}
	return closest;
}

public Pair<IntervalSet,Long> getValueIntervalsB(Long attId, Long prev, Interval interval, Interval searchInterval ) 
{
	Node na = this.db.getNodeById(attId);
	Long cate = 0L;
	Pair<IntervalSet,Long> closest = new Pair<>(null,prev);
	Iterable<Relationship> nvs = na.getRelationships( RelationshipType.withName( "Edge" ), Direction.OUTGOING );
	for (Relationship n2:nvs){       	
           	Node nv = this.db.getNodeById(n2.getEndNodeId());
           	cate = (Long) nv.getProperty("category");
           	switch (this.getOp().toLowerCase()){
           	case "up":          		    		
           		if (cate >= prev)
           			closest = getClosestPairBackwards(nv,closest,interval,searchInterval,cate);
           		break;
        	case "=":          		    		
           		if (cate == this.getValue())
           			closest = getClosestPairBackwards(nv,closest,interval,searchInterval,cate);
           		break;
        	case "sup":
        		if (cate > prev)
           			closest = getClosestPairBackwards(nv,closest,interval,searchInterval,cate);
           		break;
        	case "do":
        		if (cate <= prev)
           			closest = getClosestPairBackwards(nv,closest,interval,searchInterval,cate);
           		break;
        	case "sdo":
        		if (cate < prev)
           			closest = getClosestPairBackwards(nv,closest,interval,searchInterval,cate);
           		break;          		
           	}       	
           }

	return closest;	
}
}