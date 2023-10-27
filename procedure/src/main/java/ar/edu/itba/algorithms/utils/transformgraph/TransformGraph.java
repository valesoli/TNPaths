package ar.edu.itba.algorithms.utils.transformgraph;

import ar.edu.itba.algorithms.Condition;
import ar.edu.itba.algorithms.PayloadTransferee;
import ar.edu.itba.algorithms.utils.interval.Interval;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.logging.Log;

import java.sql.Time;
import java.util.*;

public abstract class TransformGraph<T> {

    private final Map<Long, Map<TimeNode<T>, TimeNode<T>>> vIn = new HashMap<>();
    private final Map<Long, Map<TimeNode<T>, TimeNode<T>>> vOut = new HashMap<>();

    final Log log;
    final Map<Long, SortedSet<Pair<TimeNode<T>, TimeNode<T>>>> expansions = new HashMap<>();

    protected final Comparator<TimeNode<T>> comparator;
    final PayloadTransferee<T> transferee;

    private Condition<TimeNode<T>> condition;

    public TransformGraph(
            Comparator<TimeNode<T>> comparator,
            PayloadTransferee<T> transferee,
            Log log) {
        this.comparator = comparator;
        this.transferee = transferee;
        this.log = log;
    }

    public void setCondition(Condition<TimeNode<T>> condition) {
        this.condition = condition;
    }

    protected abstract void setPayloadToNode(TimeNode<T> node, Interval interval);

    protected abstract TimeNode<T> getPreviousNode(TimeNode<T> node, Interval interval);

    private Map<TimeNode<T>, TimeNode<T>> getNodesFromMap(
            Map<Long, Map<TimeNode<T>, TimeNode<T>>> vMap, Long node) {
        if (vMap.containsKey(node)) {
            return vMap.get(node);
        }
        Map<TimeNode<T>, TimeNode<T>> map = new HashMap<>();
        vMap.put(node, map);
        return map;
    }

    private TimeNode<T> getInMap(Map<Long, Map<TimeNode<T>, TimeNode<T>>> vMap, TimeNode<T> node) {
        return getNodesFromMap(vMap, node.getNodeId()).get(node);
    }

    TimeNode<T> getVOut(TimeNode<T> node) {
        return this.getInMap(this.vOut, node);
    }

    TimeNode<T> getVIn(TimeNode<T> node) {
        return this.getInMap(this.vIn, node);
    }

    private void insertExpansion(Long id, TimeNode<T> vIn, TimeNode<T> vOut) {
        if (!expansions.containsKey(id)) {
            expansions.put(id, new TreeSet<>((p1, p2) -> {
                int comparison = p2.getLeft().getTime().compareTo(p1.getLeft().getTime());
                if (comparison == 0)
                    return p2.getRight().getNodeId().compareTo(p1.getRight().getNodeId());
                return comparison;
            }));
        }
        Set<Pair<TimeNode<T>, TimeNode<T>>> vIns = expansions.get(id);
        vIns.add(Pair.of(vOut, this.getVIn(vIn)));
    }

    public boolean isExpanded(Long id) {
        return this.expansions.containsKey(id);
    }

    private boolean getOrInsertInMap(
            Map<Long, Map<TimeNode<T>, TimeNode<T>>> vMap,
            TimeNode<T> node) {
        Map<TimeNode<T>, TimeNode<T>> nodes = getNodesFromMap(vMap, node.getNodeId());
        if (nodes.containsKey(node)) {
            if (node.isLocked()) {
                return false;
            }
            TimeNode<T> currentNode = nodes.get(node);
            int comparision = comparator.compare(node, currentNode);
            if (!currentNode.isLocked() && comparision <= 0) {
                if (comparision == 0) {
                    currentNode.previousId = node.previousId;
                    currentNode.addAllPrevious(node.getPrevious());
                    currentNode.setLength(node.getLength());
                    return this.condition.apply(node);
                }
                return false;
            }
            currentNode.previousId = node.previousId;
            currentNode.setPrevious(node.getPrevious());
            currentNode.setPayload(node.getPayload());
            currentNode.setLength(node.getLength());
            currentNode.setLocked(false);
            return true;
        }

        nodes.put(node, node);
        return !node.isLocked();
    }

    boolean getOrInsertVOut(TimeNode<T> node) {
        return this.getOrInsertInMap(this.vOut, node);
    }

    boolean getOrInsertVIn(TimeNode<T> node) {
        return this.getOrInsertInMap(this.vIn, node);
    }

    private boolean containsVIn(TimeNode<T> node) {
        if (this.vIn.containsKey(node.getNodeId())) {
            Map<TimeNode<T>, TimeNode<T>> map = this.vIn.get(node.getNodeId());
            return map.containsKey(node);
        }
        return false;
    }
    
    private List<TimeNode<T>> createNodesIfFirstNode(
            List<Interval> intervalSet, TimeNode<T> node, Long otherNode) {

        List<TimeNode<T>> frontier = new LinkedList<>();
        for(Interval interval : intervalSet) {
            TimeNode<T> vOut = new TimeNode<>(node.getNodeId(), interval.getStart(), node);
            this.setPayloadToNode(vOut, interval);
            getOrInsertVOut(vOut);

            TimeNode<T> vIn = new TimeNode<>(otherNode, interval.getEnd(), vOut);
            this.setPayloadToNode(vIn, interval);
            getOrInsertVIn(vIn);
            frontier.add(vIn);
        }
        return frontier;
    }

    public List<TimeNode<T>> getNewFrontier(List<Interval> intervalSet, TimeNode<T> node, Long otherNode) {
        if (node.getTime() == null) {
            return this.createNodesIfFirstNode(intervalSet, node, otherNode);
        }
        List<TimeNode<T>> newFrontier = new LinkedList<>();
        for (Interval interval: intervalSet) {
            TimeNode<T> previousNode = this.getPreviousNode(node, interval);

            TimeNode<T> vOut = new TimeNode<>(node.getNodeId(), interval.getStart(), previousNode);

            if (previousNode != null)
                this.transferee.transfer(previousNode, vOut);
            this.getOrInsertVOut(vOut);

            vOut = this.getVOut(vOut);

            TimeNode<T> vIn = new TimeNode<>(otherNode, interval.getEnd(), vOut, vOut.isLocked());
            vIn.previousId = vOut.getNodeId();
            this.transferee.transfer(vOut, vIn);
            boolean insertedVIn = this.getOrInsertVIn(vIn);
            vIn = this.getVIn(vIn);

            this.insertExpansion(node.getNodeId(), vIn, vOut);

            if (!vOut.isLocked() && insertedVIn) {
                vIn.priority =
                        this.isExpanded(vIn.getNodeId()) || vIn.getNodeId().equals(node.previousId) ?
                        TimeNode.LOW : TimeNode.HIGH;
                newFrontier.add(vIn);
            }
        }
        return newFrontier;
    }

    public abstract List<TimeNode<T>> getNewFrontier(TimeNode<T> node);
}
