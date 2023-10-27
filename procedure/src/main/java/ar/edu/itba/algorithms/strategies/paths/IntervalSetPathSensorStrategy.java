package ar.edu.itba.algorithms.strategies.paths;

import ar.edu.itba.algorithms.Condition;
import ar.edu.itba.algorithms.strategies.Strategy;
import ar.edu.itba.algorithms.strategies.paths.NodesIntersectionAttributePathsStrategy;
import ar.edu.itba.algorithms.utils.interval.Interval;
//import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPathSensor;
import ar.edu.itba.algorithms.utils.interval.IntervalNodeSensorPair;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.logging.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public abstract class IntervalSetPathSensorStrategy implements Strategy<IntervalNodePairPathSensor, Long> {

	protected Queue<IntervalNodePairPathSensor> frontier = new LinkedList<>();
    Queue<IntervalNodePairPathSensor> newFrontier = new LinkedList<>();
    protected final List<IntervalNodePairPathSensor> solutions = new LinkedList<>();
    protected final Long minimumLength;
    protected final Long maximumLength;
    protected Node endingNode = null;
    protected final Log log;
    protected Long nodesExpanded = 0L;
    private IntervalNodeSensorPair previousSensor;

    public IntervalSetPathSensorStrategy(Long minimumLength, Long maximumLength, Log log) {
        this.minimumLength = minimumLength;
        this.maximumLength = maximumLength;
        this.log = log;
    }

    private void logLength() {
    	IntervalNodePairPathSensor path = frontier.peek();
        if (path == null) {
            log.debug("Path length 0.");
        } else {
            log.debug("Path length %d with %d elements.",
                    frontier.peek().getLength(),
                    frontier.size());
        }
    }
    
    public IntervalNodeSensorPair getPreviousSensor() {
        return this.previousSensor;
    }
    
    @Override
    public IntervalNodePairPathSensor getNext() {
        if (frontier.isEmpty()) {
            frontier = newFrontier;
            newFrontier = new LinkedList<>();
            this.logLength();
        }
        return frontier.poll();
    }

    @Override
    public void addToFrontier(IntervalNodePairPathSensor node) {
        this.nodesExpanded += 1;
        if (node.getLength() >= minimumLength) {
            if (endingNode == null || node.getNode().equals(endingNode.getId())) {
                this.solutions.add(node);
            }
        }
        if (node.getLength() < maximumLength) {
            this.newFrontier.add(node);
        }
    }

    @Override
    public boolean isFinished() {
        return (frontier.isEmpty() && newFrontier.isEmpty());
    }

    @Override
    public void setGoal(Condition<Long> finishingCondition) {
        throw new IllegalStateException("This method is not callable for PathsStrategy.");
    }

    @Override
    public IntervalNodePairPathSensor getSolution() {
        throw new IllegalStateException("This method is not callable for PathsStrategy.");
    }

    public void setEndingNode(Node endingNode) {
        this.endingNode = endingNode;
    }

    public List<IntervalNodePairPathSensor> getSolutionPaths() {
        return this.solutions;
    }

    public abstract IntervalSet getIntervalSet(IntervalSet node, IntervalSet expandingValue);

    @Override
    public void expandFrontier(List<Interval> intervalSet, IntervalNodePairPathSensor node, Long otherNodeId) {
        if (node.isInFullPath(otherNodeId) || node.getNode().equals(otherNodeId) ||
                (this.endingNode != null && otherNodeId.equals(this.endingNode.getId()) && node.getLength() + 1 < this.minimumLength)){
            return;
        }
        IntervalSet interval = this.getIntervalSet(
                node.getIntervalSet(), new IntervalSet(intervalSet)
        );
        if (interval.isEmpty()) { return; }
        IntervalNodePairPathSensor path = new IntervalNodePairPathSensor(otherNodeId, interval, node.getCategory(), node.getLength() + 1);
        path.setPrevious(node);
        Set<Long> previousNodes = node.copyPreviousNodes();
        previousNodes.add(otherNodeId);
        path.setPreviousNodes(previousNodes);
        this.addToFrontier(path);
    }

    public Long getNodesExpanded() {
        return this.nodesExpanded;
    }
   
        
}
