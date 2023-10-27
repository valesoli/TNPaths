package ar.edu.itba.algorithms.strategies.path.consecutive;

import ar.edu.itba.algorithms.Condition;
import ar.edu.itba.algorithms.PayloadTransferee;
import ar.edu.itba.algorithms.Pruner;
import ar.edu.itba.algorithms.utils.transformgraph.TimeNode;
import ar.edu.itba.algorithms.utils.interval.Interval;
import gnu.trove.set.hash.THashSet;
import org.neo4j.logging.Log;

import java.util.*;

public class ComparatorPruneConsecutiveStrategy extends ConsecutivePathStrategy {

    private final Log log;

    PriorityQueue<TimeNode<Long>> queue;
    Set<TimeNode<Long>> nodesInQueue = new THashSet<>();

    private Condition<TimeNode<Long>> condition;
    private final Set<TimeNode<Long>> solutions = new HashSet<>();
    private TimeNode<Long> solution = null;
    private final Pruner<TimeNode<Long>> pruneCondition;

    public ComparatorPruneConsecutiveStrategy(
            Log log,
            Comparator<TimeNode<Long>> comparator,
            Pruner<TimeNode<Long>> pruneCondition,
            Long difference,
            PayloadTransferee<Long> transferee) {
        super(difference, comparator, transferee, log);
        this.log = log;
        this.pruneCondition = pruneCondition;
        this.queue = new PriorityQueue<>((t1, t2) -> {
            if (!t2.priority.equals(t1.priority))
                return t2.priority.compareTo(t1.priority);
            if (!t1.getLength().equals(t2.getLength()))
                return t2.getLength().compareTo(t1.getLength());
            int comparison = comparator.compare(t2, t1);
            if (comparison == 0)
                return t1.getTime().compareTo(t2.getTime());
            return comparison;
        });
    }

    @Override
    public TimeNode<Long> getNext() {
        TimeNode<Long> node = queue.poll();
        nodesInQueue.remove(node);
        log.debug(String.format("Took node %d with time %d length %d.",
                Objects.requireNonNull(node).getNodeId(), node.getTime(), node.getLength()));
        return node;
    }

    public void setNodeAndChangeList(TimeNode<Long> node) {
        log.info("Solution found! %s", node.toString());
        this.solution = node;
        this.solutions.clear();
        this.solutions.add(node);

        this.pruneInPriorityQueue();
    }

    private void pruneInPriorityQueue() {
        PriorityQueue<TimeNode<Long>> newQueue = new PriorityQueue<>();
        Set<TimeNode<Long>> newNodes = new THashSet<>();
        Long pruneCount = 0L;
        for (TimeNode<Long> node: this.queue) {
            if (!this.pruneCondition.prune(this.solution, node)) {
                newQueue.add(node);
                newNodes.add(node);
            } else {
                pruneCount++;
            }
        }
        log.info("Pruned %d elements.", pruneCount);
        this.queue = newQueue;
        this.nodesInQueue = newNodes;
    }

    @Override
    public void addToFrontier(TimeNode<Long> node) {
        if (this.solution != null && node.getTime() != null
                && this.pruneCondition.prune(this.solution, node)) { return; }
        this.nodesExpanded += 1;
        if (this.condition.apply(node)) {
            if (this.solution == null) {
                this.setNodeAndChangeList(node);
                return;
            }

            int result = this.comparator.compare(solution, node);
            if (result < 0) {
                this.setNodeAndChangeList(node);
            } else if (result == 0) {
                this.solutions.add(node);
            }
            return;
        }
        if (this.solution != null && this.pruneCondition.prune(this.solution, node)) {
            return;
        }

        if (!nodesInQueue.contains(node)) {
            queue.offer(node);
            nodesInQueue.add(node);
        }
    }

    @Override
    public boolean isFinished() {
        return queue.isEmpty();
    }

    @Override
    public void setGoal(Condition<TimeNode<Long>> condition) {
        this.condition = condition;
        this.graph.setCondition(condition);
    }

    @Override
    public TimeNode<Long> getSolution() {
        return this.solution;
    }

    @Override
    public void expandFrontier(List<Interval> intervalSet, TimeNode<Long> node, Long otherNode) {
        if (this.solution != null && node.getTime() != null
                && this.pruneCondition.prune(this.solution, node)) { return; }
        super.expandFrontier(intervalSet, node, otherNode);
    }

    @Override
    public void expandFrontier(TimeNode<Long> node) {
        if (this.solution != null && node.getTime() != null
                && this.pruneCondition.prune(this.solution, node)) { return; }
        super.expandFrontier(node);
    }

    @Override
    public List<TimeNode<Long>> getSolutions() {
        if (this.solutions.isEmpty())
            return null;
        return new LinkedList<>(this.solutions);
    }
}
