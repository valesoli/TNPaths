package ar.edu.itba.records.utils;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class IntervalTree {

    private List<IntervalTreeNode> sameDepthLeaves;

    public IntervalTree(Interval interval) {
        this.sameDepthLeaves = new LinkedList<>();
        this.sameDepthLeaves.add(new IntervalTreeNode(interval));
    }

    public void getNextNodes(IntervalSet intervalSet) {
        List<IntervalTreeNode> newDepthLeaves = new LinkedList<>();
        for (IntervalTreeNode node: sameDepthLeaves) {
            intervalSet.getIntervals().forEach(
                    longInterval -> newDepthLeaves.add(new IntervalTreeNode(longInterval, node))
            );
        }
        sameDepthLeaves = newDepthLeaves;
    }

    public List<List<String>> getPaths(Granularity granularity) {
        List<List<IntervalTreeNode>> paths = new ArrayList<>(sameDepthLeaves.size());
        sameDepthLeaves.forEach(unused_ -> paths.add(new LinkedList<>()));
        List<IntervalTreeNode> nodes = sameDepthLeaves;
        while (!nodes.isEmpty()) {
            List<IntervalTreeNode> nextNodes = new LinkedList<>();
            AtomicInteger i = new AtomicInteger();
            nodes.forEach(node -> {
                List<IntervalTreeNode> path = paths.get(i.get());
                path.add(node);
                if (node.getParent() != null) {
                    nextNodes.add(node.getParent());
                }
                i.getAndIncrement();
            });
            nodes = nextNodes;
        }

        return paths.stream().map(path -> path.stream().map(node -> {
                Interval interval = node.getInterval();
                return new Interval(
                        interval.getStart(),
                        interval.getEnd(),
                        granularity
                ).toString();
            }).collect(Collectors.toList())
        ).collect(Collectors.toList());
    }
}
