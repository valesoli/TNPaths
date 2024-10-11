package ar.edu.itba.records.utils;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.allenRel;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.records.AlphaResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.HashSet;

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
                    longInterval -> newDepthLeaves.add(new IntervalAlphaTreeNode(longInterval, node)));
        	
        }
        sameDepthLeaves = newDepthLeaves;
    }

    public void getNextNodes(IntervalSet intervalSet, Boolean out) {
        List<IntervalAlphaTreeNode> newDepthLeaves = new LinkedList<>();
        for (IntervalAlphaTreeNode node: sameDepthLeaves) {
            intervalSet.getIntervals().forEach(
                    longInterval -> newDepthLeaves.add(new IntervalAlphaTreeNode(longInterval, node, out)));
        
        }
        sameDepthLeaves = newDepthLeaves;
    }
    
    public AlphaResult getPaths(Granularity granularity, Boolean out) {
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

        List<List<String>> st_inters = paths.stream().map(path -> path.stream().map(node -> {
                Interval interval = node.getInterval();
                return new Interval(
                        interval.getStart(),
                        interval.getEnd(),
                        granularity
                ).toString();
            }).collect(Collectors.toList())
        ).collect(Collectors.toList());
        
        Set<List<String>> intervals = new HashSet<>();
        intervals.addAll(st_inters);
        
        
        List<List<String>> st_alphas = new ArrayList<>();
        for (List<String> st_inter:intervals) {
        	List<String> st_alpha = new ArrayList<>();
        	if (!out)
        		for (Integer i=st_inter.size()-1; i>0; i--) {
        			st_alpha.add(allenRel.getAllenfromStringInterval(st_inter.get(i),st_inter.get(i-1)));
        		}
        	else
        		for (Integer i=1; i< st_inter.size(); i++) {
        			st_alpha.add(allenRel.getAllenfromStringInterval(st_inter.get(i-1),st_inter.get(i)));
        	}
        	st_alphas.add(st_alpha);
        }
        return new AlphaResult(intervals, st_alphas);
    }
    
    
    public AlphaResult getSNAlphaPaths(Granularity granularity) {
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

        List<List<String>> st_inters = paths.stream().map(path -> path.stream().map(node -> {
                Interval interval = node.getInterval();
                return new Interval(
                        interval.getStart(),
                        interval.getEnd(),
                        granularity
                ).toString();
                
            }).collect(Collectors.toList())
        ).collect(Collectors.toList());
        
        Set<List<String>> intervals = new HashSet<>();
        intervals.addAll(st_inters);

        List<List<String>> st_alphas = new ArrayList<>();
        for (List<String> st_inter:intervals) {
        	List<String> st_alpha = new ArrayList<>();
        		for (Integer i=1; i< st_inter.size(); i++) {
        			st_alpha.add(allenRel.getAllenfromStringInterval(st_inter.get(i-1),st_inter.get(i)));
        	}
        	st_alphas.add(st_alpha);
        }
   
      return new AlphaResult(intervals,st_alphas);
    }
    
   
  
}
