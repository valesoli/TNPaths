package ar.edu.itba.records.utils;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.records.AlphaResult;
import ar.edu.itba.algorithms.utils.interval.allenRel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class IntervalAlphaTree {

    private List<IntervalAlphaTreeNode> sameDepthLeaves;

    public IntervalAlphaTree(Interval interval) {
        this.sameDepthLeaves = new LinkedList<>();
        this.sameDepthLeaves.add(new IntervalAlphaTreeNode(interval));
    }

    public void getNextNodes(IntervalSet intervalSet) {
        List<IntervalAlphaTreeNode> newDepthLeaves = new LinkedList<>();
        for (IntervalAlphaTreeNode node: sameDepthLeaves) {
            intervalSet.getIntervals().forEach(
                    longInterval -> newDepthLeaves.add(new IntervalAlphaTreeNode(longInterval, node))
            );
        }
        sameDepthLeaves = newDepthLeaves;
    }

    public AlphaResult getPaths(Granularity granularity) {
        List<List<IntervalAlphaTreeNode>> paths = new ArrayList<>(sameDepthLeaves.size());
        sameDepthLeaves.forEach(unused_ -> paths.add(new LinkedList<>()));
        List<IntervalAlphaTreeNode> nodes = sameDepthLeaves;
        while (!nodes.isEmpty()) {
            List<IntervalAlphaTreeNode> nextNodes = new LinkedList<>();
            AtomicInteger i = new AtomicInteger();
            nodes.forEach(node -> {
                List<IntervalAlphaTreeNode> path = paths.get(i.get());
                path.add(node);
                if (node.getParent() != null) {
                    nextNodes.add(node.getParent());
                }
                i.getAndIncrement();
            });
            nodes = nextNodes;
        }

        List<List<String>> st_inter = paths.stream().map(path -> path.stream().map(node -> {
                Interval interval = node.getInterval();
                return new Interval(
                        interval.getStart(),
                        interval.getEnd(),
                        granularity
                ).toString();
            }).collect(Collectors.toList())
        ).collect(Collectors.toList());
        
        List<List<String>> st_alpha = paths.stream().map(path -> path.stream().map(node -> {
            return node.getAlpha()
            ;
        }).collect(Collectors.toList())
    ).collect(Collectors.toList());
        
        return new AlphaResult(st_inter,st_alpha);
    }
}
