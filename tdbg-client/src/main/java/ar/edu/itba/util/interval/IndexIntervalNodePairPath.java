package ar.edu.itba.util.interval;

public class IndexIntervalNodePairPath extends IntervalNodePairPath{
    
    private Long tgIndexNode;

    public IndexIntervalNodePairPath(Long indexNode, Long node, IntervalSet interval, Long length) {
        super(node, interval, length);
        this.tgIndexNode = indexNode;
    }

    public Long getTgIndexNode() {
        return tgIndexNode;
    }

        /**
     * Create a new path from index node
     * Used in concat bfs algorithm
     * @param source
     * @param intermediate
     * @param destination
     * @param intervalSet
     * @param baseLength
     * @return
     */
    public static IndexIntervalNodePairPath fromTwoPath(Long indexNode, Long source, Long intermediate, Long destination, IntervalSet intervalSet, Long baseLength) {
        IndexIntervalNodePairPath currentPath = new IndexIntervalNodePairPath(indexNode, source, null, baseLength);
        IndexIntervalNodePairPath nextPath = new IndexIntervalNodePairPath(indexNode, intermediate, null, baseLength + 1);
        nextPath.setPrevious(currentPath);
        currentPath = nextPath;
        nextPath = new IndexIntervalNodePairPath(indexNode, destination,
                        intervalSet,
                        baseLength + 2);
        nextPath.setPrevious(currentPath);
        return nextPath;
    }
    
    /**
     * Extend a path from index node
     * Used in concat bfs algorithm
     * @param intermediate
     * @param destination
     * @param intervalSet
     * @return
     */
    public IndexIntervalNodePairPath extendWithTwoPath(Long indexNode, Long intermediate, Long destination, IntervalSet intervalSet) {
        IndexIntervalNodePairPath currentPath = new IndexIntervalNodePairPath(indexNode, intermediate, null, getLength() + 1);
        currentPath.setPrevious(this);
        IndexIntervalNodePairPath nextPath = new IndexIntervalNodePairPath(indexNode, destination, intervalSet, getLength() + 2);
        nextPath.setPrevious(currentPath);
        return nextPath;
    }

}
