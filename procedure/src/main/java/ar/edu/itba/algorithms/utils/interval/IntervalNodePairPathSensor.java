package ar.edu.itba.algorithms.utils.interval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IntervalNodePairPathSensor extends IntervalNodeSensorPair {
	 	private Long node;
	    private IntervalSet interval;
	    private boolean isSensor;
	    private boolean intersectPrevious = false;
	    private IntervalNodePairPathSensor previous;
	    private Long length;//sensors
	    private Long totalLength;//Segments + Sensor
	    private Set<Long> previousNodes = new HashSet<>();
	    
  public IntervalNodePairPathSensor(Long node, IntervalSet interval, boolean isSensor, Long category, Long length) {
        super(node, interval, isSensor, category);
        this.length = length;
    }
  
  public IntervalNodePairPathSensor(Long node, IntervalSet interval, boolean isSensor, Long category, Long length, Long totalLength, boolean intersects) {
      super(node, interval, isSensor, category);
      this.length = length;
      this.intersectPrevious = intersects;
      this.totalLength = totalLength;
  }

  public IntervalNodePairPathSensor(Long node, IntervalSet interval, Long category, Long length) {
      super(node, interval, category);
      this.length = length;
  }

  public IntervalNodePairPathSensor(Long node, IntervalSet interval, boolean isSensor, Long category, Long length, Long totalLength) {
      super(node, interval, isSensor,category);
      this.length = length;
      this.totalLength = totalLength;
      this.intersectPrevious = false;
  }

    public void setPrevious(IntervalNodePairPathSensor previous) {
        this.previous = previous;
    }
    
    public IntervalNodePairPathSensor getPrevious() {
        return this.previous;
    }
    
    public void setIntersectPrevious(boolean intersects){
    	this.intersectPrevious = intersects;
    }
    
    public boolean getIntersectPrevious(){
    	return this.intersectPrevious;
    }
    
    public Long getLength() {
        return length;
    }
    
    public Long getTotalLength() {
        return totalLength;
    }
    
    public void setLength(Long length) {
        this.length = length;
    }
    
    public Long getSensorLength() {
    	//return the number of Sensors in the path
        return totalLength;
    }
    
    public void setSensor(){
    	this.isSensor = true;
    }
    
    public boolean isInPath(Long node) {
        return previousNodes.contains(node);
    }
    
    public boolean isInFullPath(Long searchedNode) {
    	IntervalNodePairPathSensor curr = this;
        while (curr != null) {
            if (curr.getNode().equals(searchedNode)) return true;
            curr = curr.previous;
        }
        return false;
    }

    public Long lastSensor() {
        IntervalNodePairPathSensor curr = this;
        while (curr != null) {
            if (curr.isSensor()) return curr.getNode();
            curr = curr.previous;
        }
        return null;
    }

    public Set<Long> copyPreviousNodes() {
        return new HashSet<>(this.previousNodes);
    }

    public void deletePreviousNodes() {
        this.previousNodes = null;
    }

    public void setPreviousNodes(Set<Long> previousNodes) {
        this.previousNodes = previousNodes;
    }

    @Override
    public String toString() {
        IntervalNodePairPathSensor curr = this;
        List<Long> ids = new ArrayList<>();
        while (curr != null) {
            ids.add(curr.getNode());
            curr = curr.previous;
        }
        return ids.toString();
    }

    public boolean hasRepeated() {
        Set<Long> elements = new HashSet<>();
        for (IntervalNodePairPathSensor curr = this; curr != null; curr = curr.previous) {
            if (elements.contains(curr.getNode())) return true;
            else elements.add(curr.getNode());
        }
        return false;
    }
}
