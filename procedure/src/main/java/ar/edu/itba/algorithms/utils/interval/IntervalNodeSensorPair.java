package ar.edu.itba.algorithms.utils.interval;

import java.util.Objects;

public class IntervalNodeSensorPair {

    private final Long node;
    private IntervalSet interval;
    private boolean isSensor = false;
    private Long category;

    public IntervalNodeSensorPair(Long node, IntervalSet interval) {
        this.node = node;
        this.interval = interval;
    }
    
    public IntervalNodeSensorPair(Long node, IntervalSet interval, boolean isSensor) {
        this.node = node;
        this.interval = interval;
        this.isSensor = isSensor;
    }
    
    public IntervalNodeSensorPair(Long node, IntervalSet interval, boolean isSensor, Long category) {
        this.node = node;
        this.interval = interval;
        this.isSensor = isSensor;
        this.category = category;
    }

    public IntervalNodeSensorPair(Long node, IntervalSet interval, Long category) {
        this.node = node;
        this.interval = interval;
        this.category = category;
    }
    public Long getNode() {
        return node;
    }

    public IntervalSet getIntervalSet() {
        return interval;
    }

    public void setIntervalSet(IntervalSet interval) {
        this.interval = interval;
    }
    
    public boolean isSensor() {
    	return this.isSensor;
    }
    
    public boolean setIsSensor(Boolean iss) {
    	return this.isSensor = iss;
    }
    
    public Long getCategory() {
        return category;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.node, this.interval);
    }
    
    public String toString1(){
    	return "Impre: " + this.getNode().toString() + " " + this.getIntervalSet().toString()  + " " + this.getCategory().toString() + " " + String.valueOf(this.isSensor());
    }
}
