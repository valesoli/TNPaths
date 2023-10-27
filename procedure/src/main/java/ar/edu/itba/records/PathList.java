package ar.edu.itba.records;

import ar.edu.itba.algorithms.utils.transformgraph.TimeNode;

import java.util.Iterator;
import java.util.LinkedList;

public class PathList extends LinkedList<TimeNode<Long>> {

    @Override
    public int hashCode() {
        int hashcode = 1;
        for (TimeNode<Long> node: this) {
            hashcode *= node.getNodeId();
        }
        return hashcode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;
        PathList other = (PathList) o;
        if (other.size() != this.size()) {
            return false;
        }
        Iterator<TimeNode<Long>> first = this.listIterator();
        Iterator<TimeNode<Long>> second = other.listIterator();

        while(first.hasNext() && second.hasNext()) {
            if (!first.next().getNodeId().equals(second.next().getNodeId()))
                return false;
        }
        return true;
    }
}
