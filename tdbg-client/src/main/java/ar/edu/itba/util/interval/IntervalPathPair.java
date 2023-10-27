package ar.edu.itba.util.interval;

import java.security.InvalidAlgorithmParameterException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class IntervalPathPair {
    
    private Deque<Long> ids = new ArrayDeque<>();
    private IntervalSet intervals;

    /**
     * Create pair of (Interval, Path); Since path is of length >= 2, ids.size() must be 3. 
     * @return
     */
    public IntervalPathPair(List<Long> ids, IntervalSet interval) 
        // throws InvalidAlgorithmParameterException
    {
        // if (ids.size() != 3) throw new InvalidAlgorithmParameterException("Must begin with 3 ids");
        this.ids.addAll(ids);
        this.intervals = interval;
    }

    /**
     * 
     * @param ids new Ids, none of them present in current list
     * @param newInterval calculate new validity interval beforehand (in order to be able to discard paths before their creation)
     */
    public void addNodes(Collection<Long> ids, IntervalSet newInterval) {
        this.ids.addAll(ids);
        this.intervals = newInterval;
    }

    public void addNode(Long id, IntervalSet newInterval) {
        this.ids.add(id);
        this.intervals = newInterval;
    }

    public int getLength() {
        return ids.size();
    }

    public IntervalSet getIntervals() {
        return this.intervals;
    }
    
    public Long getLastId() {
        return ids.getLast();
    }
}
