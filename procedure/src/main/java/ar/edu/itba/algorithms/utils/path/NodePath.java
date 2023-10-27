package ar.edu.itba.algorithms.utils.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.graphdb.Node;

import ar.edu.itba.algorithms.utils.interval.Interval;

public class NodePath {

    protected List<Node> path = new ArrayList<>();
    protected List<Long> pathIds = new ArrayList<>();
    protected List<Long> pathNativeIds = new ArrayList<>();

    public NodePath() {
    }
    
    public void addNodeToPath(Node node) {
        this.path.add(node);
        this.pathIds.add((Long) node.getProperty("id"));
        this.pathNativeIds.add(node.getId());
    }

    public NodePath(List<Node> path) {
        this.path = path;
        this.pathIds = path.stream().map(x -> (Long) x.getProperty("id")).collect(Collectors.toList());
    }

    public List<Node> getPath() {
        return this.path;
    }

    @Override
    public String toString() {
        return path.stream().map(x -> (Long) x.getProperty("id")).collect(Collectors.toList()).toString();
    }

    // TODO choose one or another way
    public String nativeIdsToString() {
        return path.stream().map(x -> (Long) x.getId()).collect(Collectors.toList()).toString();
    }

    @Override
    public int hashCode() {
        return this.pathIds.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof NodePath)) return false;
        NodePath o = (NodePath) other;
        if (pathIds.size() != o.pathIds.size()) return false;
        // boolean result = true;
        // for(int i = 0; i<path.size(); i++) {
        //     if (!path.get(i).getProperty("id").equals(o.path.get(i).getProperty("id")))
        //         result = false;
        // }
        return this.pathIds.equals(o.pathIds);
        // return result;
    }
}
