package ar.edu.itba.algorithms.utils.path;

import java.util.List;

public class IdOnlyNodePath extends NodePath {
    
    public IdOnlyNodePath(List<Long> ids, List<Long> pathNativeIds) {
        this.pathIds = ids;
        this.pathNativeIds = pathNativeIds;
    }

    public IdOnlyNodePath() {
    }

    public void addNodeIdToPath(Long id) {
        this.pathIds.add(id);
    }

    public boolean containsId(Long id) {
        return this.pathIds.contains(id);
    }

    public List<Long> getIds() {
        return this.pathIds;
    }

    @Override
    public String toString() {
        return pathIds.toString();
    }
}
