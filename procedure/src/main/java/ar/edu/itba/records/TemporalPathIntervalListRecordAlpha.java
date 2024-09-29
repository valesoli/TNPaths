package ar.edu.itba.records;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPathSensor;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.records.utils.IntervalIntersectionAttributesSerializer;
import ar.edu.itba.records.utils.IntervalTree;
import ar.edu.itba.records.utils.IntervalAlphaTree;
import ar.edu.itba.records.utils.NodeSensorSerialization;
import ar.edu.itba.records.utils.NodeSerialization;
import ar.edu.itba.graph.Graph;
import ar.edu.itba.graph.impl.FullStoredGraph;


public class TemporalPathIntervalListRecordAlpha {
	public final List<List<String>> alphas;
	public final List<Map<String, Object>> path;
	public final List<List<String>> intervals;

	
	public TemporalPathIntervalListRecordAlpha(List<Map<String, Object>> path, List<List<String>> intervals, List<List<String>> alphas) {
		this.path = path;
        this.intervals = intervals;
		this.alphas=alphas;
	}
	
	public List<List<String>> getIntervals(){
		return this.intervals;
	}
	public List<List<String>> getAlphas(){
		return this.alphas;
	}
	
	public List<Map<String, Object>> getPath() {
        return path;
    }

	public static Stream<TemporalPathIntervalListRecordAlpha> getRecordsFromSolutionAlphaList(
            List<IntervalNodePairPathSensor> paths, GraphDatabaseService db, 
            Granularity granularity, String attr, Boolean dir_out,
            List<Integer> eList) {
           
		return paths.parallelStream().filter(Objects::nonNull).map(path -> {
            LinkedList<NodeSensorRecord> nodes = new LinkedList<>();
            IntervalNodePairPathSensor current = path;
            System.out.print("Cada alpha path "+ path.toString()+ " Longitud ");
            System.out.println(path.getLength());
            Long sensor_qty = path.getLength();
            Long i = sensor_qty;
            if (eList != null) {
	            while (current != null) {
	            	Boolean isSensor = false;
	            	if (current.isSensor()) {
	                    	System.out.print(i);
	                    	System.out.print(eList);
	                        System.out.print(eList.contains((int)(long)i));
	            			isSensor = (!inList(eList,(int)(long)i));
	            			--i;
	            			System.out.println(isSensor);
	            		}
	            	NodeSensorRecord nsr = new NodeSensorRecord(db.getNodeById(current.getNode()),
	            			isSensor, current.getCategory(), current.getIntervalSet()); 
	            	System.out.println("nsr con "+ current.toString1() );
	                nodes.addFirst(nsr);
	                current = current.getPrevious();
	            }
            }
            else
            	while (current != null) {
	            	NodeSensorRecord nsr = new NodeSensorRecord(db.getNodeById(current.getNode()),
	            			current.isSensor(), current.getCategory(), current.getIntervalSet()); 
	            	System.out.println("nsr "+ current.toString1() );
	                nodes.addFirst(nsr);
	                current = current.getPrevious();
	            }
            List<Map<String, Object>> serializedNodes = nodes.stream()
                    .map(node -> ((node.isSensor())? 
                    		NodeSensorSerialization.fromSensorNode(node.getNode(), 
                    				new IntervalIntersectionAttributesSerializer(path.getIntervalSet().getIntervals()),
                    				node.getCategory(),db, attr,node.getInterval()):
                            NodeSensorSerialization.fromNotSensorNode(node.getNode(),db)))
                    .collect(Collectors.toList());
                    
            return getIntervalsFromAlphaPath(path,granularity,serializedNodes, dir_out);                    
        });
    }
	
    private static TemporalPathIntervalListRecordAlpha getIntervalsFromAlphaPath(
    		IntervalNodePairPathSensor path, Granularity granularity, List<Map<String, Object>> serializedNodes,
    		Boolean dir_out) {
        int numberOfSensors = path.getLength().intValue();
        int totalLength = path.getTotalLength().intValue();
        List<IntervalAlphaTree> intervalTrees = new LinkedList<>();
        //System.out.println(path.getIntervalSet().toString());
        switch (numberOfSensors){
        case 0:
        	return new TemporalPathIntervalListRecordAlpha(serializedNodes, new LinkedList<>(), new LinkedList<>());

        case 1:
        	 path.getIntervalSet().getIntervals().forEach(
                    i -> intervalTrees.add(new IntervalAlphaTree(i)));
        	 Set<List<String>> result = new HashSet<>();
             Set<List<String>> result2 = new HashSet<>();
             for (IntervalAlphaTree intervalTree:intervalTrees) {
             	 AlphaResult intervals_and_alphas = intervalTree.getPaths(granularity,dir_out);
             	 result.addAll(intervals_and_alphas.getSt_intervals());
             	 result2.addAll(intervals_and_alphas.getSt_alphas());
             	
             }
             return new TemporalPathIntervalListRecordAlpha(serializedNodes, new LinkedList<>(result), new LinkedList<>(result2));

        default: 
        	IntervalNodePairPathSensor previousPath;
        	IntervalNodePairPathSensor previousSensor = null;
            IntervalSet previousSetSensor = null;
            
        	int sensorCount = 0;
        	if (path.isSensor()){
        		sensorCount = 1;
        		previousSensor = path;
        		previousSetSensor = path.getIntervalSet();
        		
        		//Intersect previous??
        		//if (!path.getIntersectPrevious())
        			path.getIntervalSet().getIntervals().forEach(
						i -> intervalTrees.add(new IntervalAlphaTree(i))       
				);                	
        	}
        	while ((path != null)&&(totalLength>1)) {
        	    previousPath = path.getPrevious();
                if (previousPath.isSensor()) {
                		if (sensorCount==0){
                			if (previousPath.getIntersectPrevious()){
        			 			if (previousPath.getIntervalSet() != null) {
        			 				previousPath.getIntervalSet().getIntervals().forEach(
                						i -> intervalTrees.add(new IntervalAlphaTree(i))       
                				);                			
        			 			}
                			}
                		}
                		else if (sensorCount == 1){
                			 	if (previousPath.getIntervalSet() != null) {
                			 		if (previousSensor.getIntersectPrevious()){
                			 			previousSetSensor.consecutive(previousPath.getIntervalSet()).getIntervals().forEach(
                        						i -> intervalTrees.add(new IntervalAlphaTree(i))       
                        				);    
                			 		}
                			 		else {
	                			 		IntervalSet intervals = previousPath.getIntervalSet();
	                			 		intervalTrees.forEach(intervalTree -> intervalTree.getNextNodes(intervals, dir_out));
                			 		}
                			 	}
                			}
                		else {
                			if (previousPath.getIntervalSet() != null) {
            			 			IntervalSet consecutive = previousSetSensor.consecutive(previousPath.getIntervalSet());
                			 		intervalTrees.forEach(intervalTree -> intervalTree.getNextNodes(consecutive,dir_out));
            			 		
            			 	}
                		}                			                			
                		sensorCount++;
                		previousSensor = previousPath;
                		previousSetSensor = previousPath.getIntervalSet();
                }
                path = previousPath;
                totalLength--;
        	}
        	
        }
        Set<List<String>> result = new HashSet<>();
        Set<List<String>> result2 = new HashSet<>();
        for (IntervalAlphaTree intervalTree:intervalTrees) {
        	 AlphaResult intervals_and_alphas = intervalTree.getPaths(granularity, dir_out);
        	 result.addAll(intervals_and_alphas.getSt_intervals());
        	 result2.addAll(intervals_and_alphas.getSt_alphas());
        	
        }
       
        TemporalPathIntervalListRecordAlpha res = new TemporalPathIntervalListRecordAlpha(serializedNodes, new LinkedList<>(result), new LinkedList<>(result2));
        System.out.println(res.getIntervals().toString());
        System.out.println(res.getAlphas().toString());
        return res;
    }
    
    /*SNAlphas*/
    
    public static Stream<TemporalPathIntervalListRecordAlpha> getRecordsFromSolutionSNAlphaList(
            List<IntervalNodePairPath> paths, 
            GraphDatabaseService db, 
            Granularity granularity) {
        return paths.parallelStream().filter(Objects::nonNull).map(path -> {
            LinkedList<Node> nodes = new LinkedList<>();
            IntervalNodePairPath current = path;

            while (current != null) {
                nodes.addFirst(db.getNodeById(current.getNode()));
                current = current.getPrevious();
            }
            List<Map<String, Object>> serializedNodes = nodes.stream()
                    .map(node -> NodeSerialization.fromNode(node,
                            new IntervalIntersectionAttributesSerializer(path.getIntervalSet().getIntervals()), db))
                    .collect(Collectors.toList());

            return getIntervalsFromSNAlphaPath(path, granularity,serializedNodes)
            ;
        });
    }
    
    private static TemporalPathIntervalListRecordAlpha getIntervalsFromSNAlphaPath(IntervalNodePairPath path, 
    		Granularity granularity, 
        	List<Map<String, Object>> serializedNodes)  {
        List<IntervalAlphaTree> intervalTrees = new LinkedList<>();

        IntervalNodePairPath previousPath;
        IntervalSet currentSet = path.getIntervalSet();
    	System.out.print("currentSet ");
    	System.out.println(currentSet.toString());
    
        previousPath = path.getPrevious();
        if (currentSet == null || previousPath == null) {
            return new TemporalPathIntervalListRecordAlpha(null,null,null);
        }
        currentSet.getIntervals().forEach(
                i -> intervalTrees.add(new IntervalAlphaTree(i))
        );
        path = previousPath;

       while (path != null) {
            currentSet = path.getIntervalSet();
            
            previousPath = path.getPrevious();
            if (currentSet != null ) {
            	/*&& previousPath.getIntervalSet() != null) {*/
            
            	System.out.print("currentSet ");
            	System.out.println(currentSet.toString());
               
                for (IntervalAlphaTree intervalTree:intervalTrees) {
                		intervalTree.getNextNodes(currentSet);
                }
            }
            path = previousPath;
        }

        Set<List<String>> result = new HashSet<>();
        Set<List<String>> result2 = new HashSet<>();
        
        for (IntervalAlphaTree intervalTree:intervalTrees) {
       	 
	       	 AlphaResult intervals_and_alphas = intervalTree.getSNAlphaPaths(granularity);
	       	 result.addAll(intervals_and_alphas.getSt_intervals());
	       	 result2.addAll(intervals_and_alphas.getSt_alphas());
       	
       }
       
       TemporalPathIntervalListRecordAlpha res = new TemporalPathIntervalListRecordAlpha(serializedNodes, new LinkedList<>(result), new LinkedList<>(result2));
       System.out.println(res.getIntervals().toString());
       System.out.println(res.getAlphas().toString());
       return res;
    }
    
    
    private static TemporalPathIntervalListRecordAlpha getIntervalsFromPath(IntervalNodePairPath path, 
    	Granularity granularity, 
    	List<Map<String, Object>> serializedNodes) {
        List<IntervalAlphaTree> intervalTrees = new LinkedList<>();

        IntervalNodePairPath previousPath;
        IntervalSet currentSet = path.getIntervalSet();
        previousPath = path.getPrevious();
        if (currentSet == null || previousPath == null) {
            return new TemporalPathIntervalListRecordAlpha(null,null,null);
        }
        currentSet.intersection(previousPath.getIntervalSet()).getIntervals().forEach(
                i -> intervalTrees.add(new IntervalAlphaTree(i))
        );
        path = previousPath;

        while (path != null) {
            currentSet = path.getIntervalSet();
            previousPath = path.getPrevious();
            if (currentSet != null && previousPath.getIntervalSet() != null) {
                IntervalSet intersection = currentSet.intersection(currentSet);
                intervalTrees.forEach(intervalTree -> intervalTree.getNextNodes(intersection));
            }
            path = previousPath;
        }
        
        Set<List<String>> result = new HashSet<>();
        Set<List<String>> result2 = new HashSet<>();
        for (IntervalAlphaTree intervalTree:intervalTrees) {
        	 
        	 AlphaResult intervals_and_alphas = intervalTree.getPaths(granularity,true);
        	 result.addAll(intervals_and_alphas.getSt_intervals());
        	 result2.addAll(intervals_and_alphas.getSt_alphas());
        	
        }

        TemporalPathIntervalListRecordAlpha res = new TemporalPathIntervalListRecordAlpha(serializedNodes, new LinkedList<>(result), new LinkedList<>(result2));
        System.out.println(res.getIntervals().toString());
        System.out.println(res.getAlphas().toString());
        return res;
        
        /*intervalTrees.forEach(intervalTree -> result.addAll(intervalTree.getPaths(granularity)));
        return new LinkedList<>(result);*/
    }

    private static Boolean inList(List<Integer> list, Integer val) {
    	for (Integer i:list)
    		if (i==val)
    			return true;
    	return false;
    }
    
    public static void main(String[] args) {
    	List<Integer> a = new ArrayList<>(Arrays.asList(2,3,4,5));
    	System.out.println(inList(a,(int)(long)2L));
    }   	
}
