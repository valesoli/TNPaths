package ar.edu.itba.algorithms.utils.transformgraph;

import ar.edu.itba.algorithms.PayloadTransferee;
import ar.edu.itba.algorithms.utils.interval.Interval;
import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.logging.Log;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class LongTransformGraph extends TransformGraph<Long>{

    private final Long difference;
    private final TimeNode<Long> vO_cache = new TimeNode<>(null, null, null);
    private final TimeNode<Long> vI_cache = new TimeNode<>(null, null, null);

    public LongTransformGraph(
            Long difference,
            Comparator<TimeNode<Long>> comparator,
            PayloadTransferee<Long> transferee,
            Log log) {
        super(comparator, transferee, log);
        this.difference = difference;
    }

    @Override
    protected void setPayloadToNode(TimeNode<Long> node, Interval interval) {
        node.setPayload(interval.getStart());
    }

    @Override
    protected TimeNode<Long> getPreviousNode(TimeNode<Long> node, Interval interval) {
        Long diffTime = node.getTime() + this.difference;
        return interval.getStart() < diffTime ?
                null : node;
    }

    private void expandFrontierAux(
            List<TimeNode<Long>> frontier,
            TimeNode<Long> node,
            TimeNode<Long> vO,
            TimeNode<Long> vI) {

        this.vO_cache.copy(vO);
        this.vO_cache.addPrevious(node);
        this.vO_cache.setLength(node.getLength() + 1);
        this.vO_cache.setLocked(false);
        this.transferee.transfer(node, this.vO_cache);
        this.getOrInsertVOut(this.vO_cache);

        this.vI_cache.copy(vI);
        this.vI_cache.addPrevious(vO);
        this.vI_cache.setLocked(vO.isLocked());
        this.vI_cache.setLength(vO.getLength() + 1);
        this.vI_cache.previousId = this.vO_cache.getNodeId();
        this.transferee.transfer(vO, this.vI_cache);
        boolean insertedVIn = this.getOrInsertVIn(this.vI_cache);

        if (insertedVIn) {
            vI.priority =
                    this.isExpanded(vI.getNodeId()) || vI.getNodeId().equals(node.previousId) ?
                TimeNode.LOW : TimeNode.HIGH;
            frontier.add(vI);
        }
    }

    @Override
    public List<TimeNode<Long>> getNewFrontier(TimeNode<Long> node) {
        Set<Pair<TimeNode<Long>, TimeNode<Long>>> expansions = this.expansions.get(node.getNodeId());
        List<TimeNode<Long>> frontier = new LinkedList<>();
        for (Pair<TimeNode<Long>, TimeNode<Long>> expandedNodes : expansions) {
            TimeNode<Long> vO = expandedNodes.getLeft();
            TimeNode<Long> vI = expandedNodes.getRight();
            if (vO.getTime() <= node.getTime() + difference) {
                break;
            }
            this.expandFrontierAux(frontier, node, vO, vI);
        }
        return frontier;
    }

}
