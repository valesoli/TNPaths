package ar.edu.itba.records;

import org.neo4j.graphdb.Node;

import ar.edu.itba.algorithms.utils.interval.IntervalSet;

public class NodeSensorRecord {

    public final Node node;
    public final boolean isSensor;
    public final Long category;
    public final IntervalSet is;

     
    public NodeSensorRecord(Node node, boolean is, Long category) {
        this.node = node;
        this.isSensor = is;
        this.category = category;
        this.is = null;
    }
    
    public NodeSensorRecord(Node node, boolean is, Long category, IntervalSet interval) {
        this.node = node;
        this.isSensor = is;
        this.category = category;
        this.is = interval;
    }
    
    public boolean isSensor(){
    	return this.isSensor;
    }
    
   public Node getNode(){
	   return this.node;
   }
   
   public Long getCategory(){
	   return this.category;
   }
   
   public IntervalSet getInterval(){
	   return this.is;
   }
   public String toString1() {
	   
		return "NodeSensor: " + this.node.toString() + " is Sensor: " 
		+ this.isSensor() ;
}
   
}
