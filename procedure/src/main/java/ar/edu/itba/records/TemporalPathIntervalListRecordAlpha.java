package ar.edu.itba.records;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPathSensor;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.records.utils.IntervalIntersectionAttributesSerializer;
import ar.edu.itba.records.utils.IntervalAlphaTree;
import ar.edu.itba.records.utils.NodeSensorSerialization;


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
            List<IntervalNodePairPathSensor> paths, GraphDatabaseService db, Granularity granularity, String attr) {
        return paths.parallelStream().filter(Objects::nonNull).map(path -> {
            LinkedList<NodeSensorRecord> nodes = new LinkedList<>();
            IntervalNodePairPathSensor current = path;
            System.out.println("Cada alpha path "+ path.toString());
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
                    
            return getIntervalsFromAlphaPath(path,granularity,serializedNodes);                    
        });
    }
    private static TemporalPathIntervalListRecordAlpha getIntervalsFromAlphaPath(
    		IntervalNodePairPathSensor path, Granularity granularity, List<Map<String, Object>> serializedNodes ) {
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
             	 AlphaResult intervals_and_alphas = intervalTree.getPaths(granularity);
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
	                			 		intervalTrees.forEach(intervalTree -> intervalTree.getNextNodes(intervals));
                			 		}
                			 	}
                			}
                		else {
                			if (previousPath.getIntervalSet() != null) {
            			 			IntervalSet consecutive = previousSetSensor.consecutive(previousPath.getIntervalSet());
                			 		intervalTrees.forEach(intervalTree -> intervalTree.getNextNodes(consecutive));
            			 		
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
        	 AlphaResult intervals_and_alphas = intervalTree.getPaths(granularity);
        	 result.addAll(intervals_and_alphas.getSt_intervals());
        	 result2.addAll(intervals_and_alphas.getSt_alphas());
        	
        }
       
        TemporalPathIntervalListRecordAlpha res = new TemporalPathIntervalListRecordAlpha(serializedNodes, new LinkedList<>(result), new LinkedList<>(result2));
        System.out.println(res.getIntervals().toString());
        System.out.println(res.getAlphas().toString());
        return res;
    }

}
