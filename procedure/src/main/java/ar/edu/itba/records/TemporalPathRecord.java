package ar.edu.itba.records;

import ar.edu.itba.algorithms.utils.transformgraph.TimeNode;
import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.path.IdOnlyNodePath;
import ar.edu.itba.algorithms.utils.path.NodePath;
import ar.edu.itba.records.utils.IntervalIntersectionAttributesSerializer;
import ar.edu.itba.records.utils.NodeSerialization;
import ar.edu.itba.records.utils.AllAttributesSerializer;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Label;
import org.neo4j.logging.Log;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemporalPathRecord {

    public List<Map<String, Object>> path;
    public List<String> interval;

    public TemporalPathRecord(List<Map<String, Object>> path, List<String> interval) {
        this.path = path;
        this.interval = interval;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TemporalPathRecord path = (TemporalPathRecord) obj;
        return this.interval.equals(path.interval) && this.checkPaths(path.path);
    }

    @Override
    public int hashCode() {
        long hashcode = 1L;
        for (Map<String, Object> map: this.path) {
            for (String interval: ((String[]) map.get("interval"))) {
                hashcode = hashcode * interval.hashCode();
            }
        }
        return Long.hashCode(hashcode) * this.interval.hashCode();
    }

    private boolean checkPaths(List<Map<String, Object>> path) {
        if (path.size() != this.path.size()) {
            return false;
        }
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i).get("id") != this.path.get(i).get("id")) {
                return false;
            }
        }
        return true;
    }

    public List<Map<String, Object>> getPath() {
        return path;
    }

    public List<String> getInterval() {
        return interval;
    }

    private static int pruneTree(TimeNode<Long> node) {
        if (node.getTime() == null) {
            return 1;
        }
        int leaves = 0;
        for (TimeNode<Long> n: node.getPrevious()) {
            int result = pruneTree(n);
            leaves += result;
        }
        node.setPathsNumber(leaves);
        return leaves;
    }

    private static List<PathList> createListOfPaths(int number) {
        List<PathList> paths = new ArrayList<>(number);
        for (int i = 0; i < number; i++)
            paths.add(new PathList());
        return paths;
    }

    private static List<PathList> getPaths(TimeNode<Long> node, int number) {
        List<PathList> paths = createListOfPaths(number);
        getPaths_(node, 0, number - 1, paths);
        return paths;
    }

    private static void getPaths_(
            TimeNode<Long> node, int minIndex, int maxIndex, List<PathList> paths) {
        if (node.getPrevious().size() == 0) {
            return;
        }
        for (int i = minIndex; i <= maxIndex; i++) {
            paths.get(i).add(node);
        }
        int index = minIndex;
        for (TimeNode<Long> n: node.getPrevious()) {
            getPaths_(n, index, index + n.getPathsNumber() - 1, paths);
            index += n.getPathsNumber();
        }
    }

    private static List<Map<String, Object>> getNodesPath(List<TimeNode<Long>> path, GraphDatabaseService db) {
        List<Map<String, Object>> list = new LinkedList<>();
        Long previousId = null;
        for (TimeNode<Long> node: path) {
            if (!node.getNodeId().equals(previousId)) {
                previousId = node.getNodeId();
                list.add(NodeSerialization.fromNode(db.getNodeById(previousId), new AllAttributesSerializer(), db));
            }
        }
        Collections.reverse(list);
        return list;
    }

    private static String getIntervalFromPath(List<TimeNode<Long>> path, Granularity granularity) {
        Long initialTime = path.get(path.size() - 1).getTime();
        Long finalTime = path.get(0).getTime();
        return new Interval(initialTime, finalTime, granularity).toString();
    }

    public static Stream<TemporalPathRecord> getRecordsFromTimeNodeList(
            List<TimeNode<Long>> solutions, GraphDatabaseService db, Granularity granularity, Log log) {
        if (solutions == null || solutions.isEmpty()) {
            return Stream.empty();
        }
        Map<PathList, Set<String>> allSolutions = new THashMap<>();

        for (TimeNode<Long> solution: solutions) {
            log.info("Counting leafs");
            int numberOfPaths = pruneTree(solution);
            log.info("Processing solution with %d results", numberOfPaths);
            List<PathList> paths = getPaths(solution, numberOfPaths);
            log.info("Finishing processing solutions for this tree. There was %d solutions", paths.size());
            for (PathList path: paths) {
                String interval = getIntervalFromPath(path, granularity);
                if (allSolutions.containsKey(path)) {
                    allSolutions.get(path).add(interval);
                } else {
                    Set<String> intervals = new THashSet<>();
                    intervals.add(interval);
                    allSolutions.put(path, intervals);
                }
            }
        }

        return allSolutions.entrySet().stream().map(
                path -> new TemporalPathRecord(
                        getNodesPath(path.getKey(), db),
                        new LinkedList<>(path.getValue())
                )
        );
    }

    public static Stream<TemporalPathRecord> getRecordsFromSolutionList(
            List<IntervalNodePairPath> paths, GraphDatabaseService db, Granularity granularity) {
        return paths.parallelStream().flatMap(path -> {
            LinkedList<Node> nodes = new LinkedList<>();
            IntervalNodePairPath current = path;
            while (current != null) {
                nodes.addFirst(db.getNodeById(current.getNode()));
                current = current.getPrevious();
            }

            List<String> intervals = path.getIntervalSet().getIntervals().stream().map(
                    // interval -> new Interval(interval.getStart(), interval.getEnd(), granularity, interval.isNow()).toString()
                    interval -> interval.toString()
            ).distinct().collect(Collectors.toList());

            List<Map<String, Object>> serializedNodes = nodes.stream()
                    .map(node -> NodeSerialization.fromNode(node,
                            new IntervalIntersectionAttributesSerializer(path.getIntervalSet().getIntervals()), db))
                    .collect(Collectors.toList());

            return Stream.of(new TemporalPathRecord(serializedNodes, intervals));
        });
    }

    public static Stream<TemporalPathRecord> getRecordsFromSolutionSensorList(
            List<IntervalNodePairPath> paths, GraphDatabaseService db, Granularity granularity) {
        return paths.parallelStream().flatMap(path -> {
            LinkedList<Node> nodes = new LinkedList<>();
            IntervalNodePairPath current = path;
            while (current != null) {
                nodes.addFirst(db.getNodeById(current.getNode()));
                current = current.getPrevious();
            }

            List<String> intervals = path.getIntervalSet().getIntervals().stream().map(
                    // interval -> new Interval(interval.getStart(), interval.getEnd(), granularity, interval.isNow()).toString()
                    interval -> interval.toString()
            ).distinct().collect(Collectors.toList());

            List<Map<String, Object>> serializedNodes = nodes.stream()
                    .map(node -> ( (node.hasProperty("name"))? NodeSerialization.fromSensorNode(node,
                            new IntervalIntersectionAttributesSerializer(path.getIntervalSet().getIntervals()), db):
                    		NodeSerialization.fromNotSensorNode(node,
                                    new IntervalIntersectionAttributesSerializer(path.getIntervalSet().getIntervals()), db)))
                    .collect(Collectors.toList());

            return Stream.of(new TemporalPathRecord(serializedNodes, intervals));
        });
    }


    // TODO avoid code repetition later
    public static Stream<TemporalPathRecord> getRecordsFromSolutionListByPropertyId(
        List<IntervalNodePairPath> paths, GraphDatabaseService db, Granularity granularity, Log log) {
    // log.info("getRecordsFromSolutionsListByPropertyId\n");
    return paths.parallelStream().flatMap(path -> {
        LinkedList<Node> nodes = new LinkedList<>();
        IntervalNodePairPath current = path;
        while (current != null) {
            nodes.addFirst(db.findNode(Label.label("Object"), "id", current.getNode()));
            current = current.getPrevious();
        }

        List<String> intervals = path.getIntervalSet().getIntervals().stream().map(
                // interval -> new Interval(interval.getStart(), interval.getEnd(), granularity, interval.isNow()).toString()
                interval -> interval.toString()
        ).distinct().collect(Collectors.toList());

        List<Map<String, Object>> serializedNodes = nodes.stream()
                .map(node -> NodeSerialization.fromNode(node,
                        new IntervalIntersectionAttributesSerializer(path.getIntervalSet().getIntervals()), db))
                .collect(Collectors.toList());

        return Stream.of(new TemporalPathRecord(serializedNodes, intervals));
    });
}

public static Map<NodePath, List<Interval>> getSolutionsMapFromSolutionsList(
    List<IntervalNodePairPath> paths, GraphDatabaseService db, Granularity granularity, Log log) {
    // log.info("getRecordsFromSolutionsListByPropertyId\n");
    System.out.println("Got " + paths.size() + " solutions");
    Map<NodePath, List<Interval>> solutionsMap = new HashMap<>();
    paths.stream().forEach(path -> {
        LinkedList<Long> nodeIds = new LinkedList<>();
        IntervalNodePairPath current = path;
        while (current != null) {
            nodeIds.addFirst(current.getNode());
            current = current.getPrevious();
        }
        solutionsMap.put(new IdOnlyNodePath(nodeIds, null), path.getIntervalSet().getIntervals());
    });
    System.out.println("Map with " + solutionsMap.keySet().size() + " keys");
    return solutionsMap;
    }


    public static Stream<TemporalPathRecord> getRecordsFromSolutionMap(
        Map<NodePath, List<Interval>> paths, GraphDatabaseService db,
        Granularity granularity
    ) {
        return paths.keySet().stream().flatMap(key -> {

            List<Interval> intervals = paths.get(key);
            List<String> intervalStrings = intervals.stream().map(interval -> new Interval(interval.getStart(), interval.getEnd(), interval.getGranularity(), interval.isNow()).toString()
            ).distinct().collect(Collectors.toList());
            List<Map<String, Object>> serializedNodes;
            if (!(key instanceof IdOnlyNodePath)) {
                serializedNodes = key.getPath().stream()
                .map(node -> NodeSerialization.fromNode(node,
                        new IntervalIntersectionAttributesSerializer(intervals), db))
                .collect(Collectors.toList());
            } else {
                serializedNodes = ((IdOnlyNodePath) key).getIds().stream()
                .map(id -> NodeSerialization.fromNodeIdProperty(id,
                        new IntervalIntersectionAttributesSerializer(intervals), db))
                .collect(Collectors.toList());
            }

            return Stream.of(new TemporalPathRecord(serializedNodes, intervalStrings));
        });
    }
}
