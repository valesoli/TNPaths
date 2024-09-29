package ar.edu.itba.algorithms.utils.interval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IntervalNodePairPath extends IntervalNodePair {

    private Long node;
    private IntervalSet interval;
    private IntervalNodePairPath previous;
    private Long length;
    private Set<Long> previousNodes = new HashSet<>();

    public IntervalNodePairPath(Long node, IntervalSet interval, Long length) {
        super(node, interval);
        this.length = length;
    }


    public void setPrevious(IntervalNodePairPath previous) {
        this.previous = previous;
    }
    
    public IntervalNodePairPath getPrevious() {
        return this.previous;
    }
    
    public Long getLength() {
        return length;
    }
    
    public void setLength(Long length) {
        this.length = length;
    }
    
    public boolean isInPath(Long node) {
        return previousNodes.contains(node);
    }

    public boolean isInFullPath(Long searchedNode) {
        IntervalNodePairPath curr = this;
        while (curr != null) {
            if (curr.getNode().equals(searchedNode)) return true;
            curr = curr.previous;
        }
        return false;
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
        IntervalNodePairPath curr = this;
        List<Long> ids = new ArrayList<>();
        while (curr != null) {
            ids.add(curr.getNode());
            curr = curr.previous;
        }
        return ids.toString();
    }


    public boolean hasRepeated() {
        Set<Long> elements = new HashSet<>();
        for (IntervalNodePairPath curr = this; curr != null; curr = curr.previous) {
            if (elements.contains(curr.getNode())) return true;
            else elements.add(curr.getNode());
        }
        return false;
    }
}
