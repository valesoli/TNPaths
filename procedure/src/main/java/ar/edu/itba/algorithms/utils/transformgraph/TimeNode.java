package ar.edu.itba.algorithms.utils.transformgraph;

import gnu.trove.set.hash.THashSet;

import java.util.*;

public class TimeNode<T> implements Comparable{

    private Long time;
    private Long nodeId;
    private final Set<TimeNode<T>> previous = new THashSet<>();
    private T payload;
    private Long length = 0L;

    private int pathsNumber = 0;
    public Long priority = LOW;
    public Long previousId;

    private boolean isLocked = false;

    public static final Long HIGH = 1L;
    public static final Long LOW = 0L;

    public TimeNode(Long nodeId, Long time) {
        this(nodeId, time, null);
    }

    public TimeNode(Long nodeId, Long time, TimeNode<T> previous) {
        this.nodeId = nodeId;
        this.time = time;
        if (previous == null) {
            this.isLocked = this.time != null;
            return;
        }
        this.previous.add(previous);
        this.length = previous.getLength() + 1;
    }

    public TimeNode(Long nodeId, Long time, TimeNode<T> previous, boolean isLocked) {
        this.nodeId = nodeId;
        this.time = time;
        if (previous == null) {
            this.isLocked = true;
            return;
        }
        this.isLocked = isLocked;
        this.previous.add(previous);
        this.length = previous.getLength() + 1;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean lock) {
        this.isLocked = lock;
    }

    public Long getTime() {
        return time;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public Set<TimeNode<T>> getPrevious() {
        return previous;
    }

    public void setPrevious(Set<TimeNode<T>> previous) {
        this.previous.clear();
        this.addAllPrevious(previous);
    }

    public void addPrevious(TimeNode<T> previous) {
        this.previous.add(previous);
    }

    public void addAllPrevious(Set<TimeNode<T>> previous) {
        this.previous.addAll(previous);
        if (this.previous.size() != 1) {
            this.isLocked = false;
        }
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof TimeNode)) {
            return -1;
        }
        TimeNode node = (TimeNode) o;
        if (node.time.getClass() != this.time.getClass()) {
            throw new IllegalStateException("Classes do not match!");
        }
        return this.time.compareTo(node.time);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TimeNode)) {
            return false;
        }
        TimeNode other = (TimeNode) o;
        if (other.nodeId.equals(this.nodeId)) {
            if (other.time == null)
                return this.time == null;
            return other.time.equals(this.time);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.nodeId, this.time);
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public int getPathsNumber() {
        return pathsNumber;
    }

    public void setPathsNumber(int pathsNumber) {
        this.pathsNumber = pathsNumber;
    }

    public void copy(TimeNode<T> other) {
        this.time = other.time;
        this.nodeId = other.nodeId;
        this.previous.clear();
        this.isLocked = other.isLocked;
    }
}
