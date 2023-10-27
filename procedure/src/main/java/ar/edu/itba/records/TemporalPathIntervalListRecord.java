package ar.edu.itba.records;

import ar.edu.itba.algorithms.utils.interval.*;
import ar.edu.itba.records.utils.IntervalIntersectionAttributesSerializer;
import ar.edu.itba.records.utils.IntervalTree;
import ar.edu.itba.records.utils.NodeSensorSerialization;
import ar.edu.itba.records.utils.NodeSerialization;
import ar.edu.itba.records.NodeSensorRecord;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemporalPathIntervalListRecord {

    public final List<Map<String, Object>> path;
    public final List<List<String>> intervals;

    public TemporalPathIntervalListRecord(List<Map<String, Object>> path, List<List<String>> intervals) {
        this.path = path;
        this.intervals = intervals;
    }

    public List<Map<String, Object>> getPath() {
        return path;
    }

    public List<List<String>> getIntervals() {
        return intervals;
    }

    private static List<List<String>> getIntervalsFromPath(IntervalNodePairPath path, Granularity granularity) {
        List<IntervalTree> intervalTrees = new LinkedList<>();

        IntervalNodePairPath previousPath;
        IntervalSet currentSet = path.getIntervalSet();
        previousPath = path.getPrevious();
        if (currentSet == null || previousPath == null) {
            return new LinkedList<>();
        }
        currentSet.intersection(previousPath.getIntervalSet()).getIntervals().forEach(
                i -> intervalTrees.add(new IntervalTree(i))
        );
        path = previousPath;

        while (path != null) {
            currentSet = path.getIntervalSet();
            previousPath = path.getPrevious();
            if (currentSet != null && previousPath.getIntervalSet() != null) {
                IntervalSet intersection = currentSet.intersection(previousPath.getIntervalSet());
                intervalTrees.forEach(intervalTree -> intervalTree.getNextNodes(intersection));
            }
            path = previousPath;
        }

        Set<List<String>> result = new HashSet<>();
        intervalTrees.forEach(intervalTree -> result.addAll(intervalTree.getPaths(granularity)));
        return new LinkedList<>(result);
    }
    
    private static List<List<String>> getIntervalsFromSensorPath(IntervalNodePairPathSensor path, Granularity granularity) {
        int numberOfSensors = path.getLength().intValue();
        int totalLength = path.getTotalLength().intValue();
        List<IntervalTree> intervalTrees = new LinkedList<>();
        switch (numberOfSensors){
        case 0:
        	return new LinkedList<>();
       
        case 1:
        	 path.getIntervalSet().getIntervals().forEach(
                    i -> intervalTrees.add(new IntervalTree(i)));
        	 Set<List<String>> result = new HashSet<>();
             intervalTrees.forEach(intervalTree -> result.addAll(intervalTree.getPaths(granularity)));
             return new LinkedList<>(result);
             
        default: 
        	IntervalNodePairPathSensor previousPath;
        	IntervalNodePairPathSensor previousSensor = null;
            IntervalSet previousSetSensor = null;
            
        	int sensorCount = 0;
        	if (path.isSensor()){
        		sensorCount = 1;
        		previousSensor = path;
        		previousSetSensor = path.getIntervalSet();
        	}
        	while ((path != null)&&(totalLength>1)) {
        	    previousPath = path.getPrevious();
                if (previousPath.isSensor()) {
                		if (sensorCount==1){
                			if (previousPath.getIntervalSet() != null) {
                				previousSetSensor.intersection(previousPath.getIntervalSet()).getIntervals().forEach(
                						i -> intervalTrees.add(new IntervalTree(i))       
                				);                			
                			}}
                		else if (sensorCount > 1){
                			 	if (previousPath.getIntervalSet() != null) {
                			 		IntervalSet intersection = previousSetSensor.intersection(previousPath.getIntervalSet());
                			 		intervalTrees.forEach(intervalTree -> intervalTree.getNextNodes(intersection));                			 		
             	            }
                		}
                		sensorCount++;
                		previousSensor = previousPath;
                		previousSetSensor = previousPath.getIntervalSet();
                }
                path = previousPath;
                totalLength--;
                System.out.println(previousPath.toString());
        	}
        	
        }
        Set<List<String>> result = new HashSet<>();
        intervalTrees.forEach(intervalTree -> result.addAll(intervalTree.getPaths(granularity)));
        return new LinkedList<>(result);
    }

    private static List<List<String>> getIntervalsFromSensorConsecutivePath(
    		IntervalNodePairPathSensor path, Granularity granularity) {
        int numberOfSensors = path.getLength().intValue();
        int totalLength = path.getTotalLength().intValue();
        List<IntervalTree> intervalTrees = new LinkedList<>();
        System.out.println("number of sensors"+String.valueOf(numberOfSensors));
        switch (numberOfSensors){
        case 0:
        	return new LinkedList<>();
       
        case 1:
        	 path.getIntervalSet().getIntervals().forEach(
                    i -> intervalTrees.add(new IntervalTree(i)));
        	 Set<List<String>> result = new HashSet<>();
             intervalTrees.forEach(intervalTree -> result.addAll(intervalTree.getPaths(granularity)));
             return new LinkedList<>(result);
             
        default: 
        	IntervalNodePairPathSensor previousPath;
        	IntervalNodePairPathSensor previousSensor = null;
            IntervalSet previousSetSensor = null;
            
        	int sensorCount = 0;
        	if (path.isSensor()){
        		sensorCount = 1;
        		previousSensor = path;
        		previousSetSensor = path.getIntervalSet();
        		path.getIntervalSet().getIntervals().forEach(
						i -> intervalTrees.add(new IntervalTree(i))       
				);                	
        	}
        	while ((path != null)&&(totalLength>1)) {
        	    previousPath = path.getPrevious();
                if (previousPath.isSensor()) {
                		if (sensorCount==0){
                			if (previousPath.getIntervalSet() != null) {
                				previousPath.getIntervalSet().getIntervals().forEach(
                						i -> intervalTrees.add(new IntervalTree(i))       
                				);                			
                			}}
                		else if (sensorCount >= 1){
                			 	if (previousPath.getIntervalSet() != null) {
                			 		IntervalSet intervals = previousPath.getIntervalSet();
                			 		intervalTrees.forEach(intervalTree -> intervalTree.getNextNodes(intervals));                			 		
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
        intervalTrees.forEach(intervalTree -> result.addAll(intervalTree.getPaths(granularity)));
        return new LinkedList<>(result);
    }
    private static List<List<String>> getIntervalsFromSensorFlowingPath(
    		IntervalNodePairPathSensor path, Granularity granularity) {
        int numberOfSensors = path.getLength().intValue();
        int totalLength = path.getTotalLength().intValue();
        List<IntervalTree> intervalTrees = new LinkedList<>();
        //System.out.println(path.getIntervalSet().toString());
        switch (numberOfSensors){
        case 0:
        	return new LinkedList<>();
       
        case 1:
        	 path.getIntervalSet().getIntervals().forEach(
                    i -> intervalTrees.add(new IntervalTree(i)));
        	 Set<List<String>> result = new HashSet<>();
             intervalTrees.forEach(intervalTree -> result.addAll(intervalTree.getPaths(granularity)));
             return new LinkedList<>(result);
             
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
						i -> intervalTrees.add(new IntervalTree(i))       
				);                	
        	}
        	while ((path != null)&&(totalLength>1)) {
        	    previousPath = path.getPrevious();
                if (previousPath.isSensor()) {
                		if (sensorCount==0){
                			if (previousPath.getIntersectPrevious()){
        			 			if (previousPath.getIntervalSet() != null) {
        			 				previousPath.getIntervalSet().getIntervals().forEach(
                						i -> intervalTrees.add(new IntervalTree(i))       
                				);                			
        			 			}
                			}
                		}
                		else if (sensorCount == 1){
                			 	if (previousPath.getIntervalSet() != null) {
                			 		if (previousSensor.getIntersectPrevious()){
                			 			previousSetSensor.consecutive(previousPath.getIntervalSet()).getIntervals().forEach(
                        						i -> intervalTrees.add(new IntervalTree(i))       
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
        intervalTrees.forEach(intervalTree -> result.addAll(intervalTree.getPaths(granularity)));
        return new LinkedList<>(result);
    }

    public static Stream<TemporalPathIntervalListRecord> getRecordsFromSolutionList(
            List<IntervalNodePairPath> paths, GraphDatabaseService db, Granularity granularity) {
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

            return new TemporalPathIntervalListRecord(
                    serializedNodes,
                    getIntervalsFromPath(path, granularity)
            );
        });
    }
    
    public static Stream<TemporalPathIntervalListRecord> getRecordsFromSolutionSensorList(
            List<IntervalNodePairPathSensor> paths, GraphDatabaseService db, Granularity granularity) {
        return paths.parallelStream().filter(Objects::nonNull).map(path -> {
            LinkedList<NodeSensorRecord> nodes = new LinkedList<>();
            IntervalNodePairPathSensor current = path;
            System.out.println("Cada path "+ path.toString());
            while (current != null) {
                nodes.addFirst(new NodeSensorRecord(db.getNodeById(current.getNode()),current.isSensor(), current.getCategory()));
                current = current.getPrevious();
            }
            
            List<Map<String, Object>> serializedNodes = nodes.stream()
                    .map(node -> ((node.isSensor())? NodeSensorSerialization.fromNode(node.getNode(),
                            new IntervalIntersectionAttributesSerializer(path.getIntervalSet().getIntervals()), db):
                            	NodeSensorSerialization.fromNotSensorNode(node.getNode(),db)))
                    .collect(Collectors.toList());
            System.out.println("Sale del while "+path.toString());
            return new TemporalPathIntervalListRecord(
                    serializedNodes,
                    getIntervalsFromSensorPath(path, granularity)
            );
        });
    }
    
    public static Stream<TemporalPathIntervalListRecord> getRecordsFromSolutionConsecutiveSensorList(
            List<IntervalNodePairPathSensor> paths, GraphDatabaseService db, Granularity granularity) {
        return paths.parallelStream().filter(Objects::nonNull).map(path -> {
            LinkedList<NodeSensorRecord> nodes = new LinkedList<>();
            IntervalNodePairPathSensor current = path;
            System.out.println("Consecutive "+ path.toString());
            while (current != null) {
                nodes.addFirst(new NodeSensorRecord(db.getNodeById(current.getNode()),
                		current.isSensor(),
                		current.getCategory()));
                current = current.getPrevious();
            }
            System.out.println("Sale del while "+nodes.toString());
            List<Map<String, Object>> serializedNodes = nodes.stream()
                    .map(node -> ((node.isSensor())? NodeSensorSerialization.fromNode(node.getNode(),
                            new IntervalIntersectionAttributesSerializer(path.getIntervalSet().getIntervals()), db)
                    		:NodeSensorSerialization.fromNotSensorNode(node.getNode(),db)))
                    .collect(Collectors.toList());

            return new TemporalPathIntervalListRecord(
                    serializedNodes,
                    getIntervalsFromSensorConsecutivePath(path, granularity)
            );
        });
    }
    public static Stream<TemporalPathIntervalListRecord> getRecordsFromSolutionFlowingSensorList(
            List<IntervalNodePairPathSensor> paths, GraphDatabaseService db, Granularity granularity, String attr) {
        return paths.parallelStream().filter(Objects::nonNull).map(path -> {
            LinkedList<NodeSensorRecord> nodes = new LinkedList<>();
            IntervalNodePairPathSensor current = path;
            System.out.println("Cada path "+ path.toString());
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

            return new TemporalPathIntervalListRecord(
                    serializedNodes,
                    getIntervalsFromSensorFlowingPath(path, granularity)
            );
        });
    }
}
