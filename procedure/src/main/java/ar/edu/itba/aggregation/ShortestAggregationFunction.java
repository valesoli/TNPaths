package ar.edu.itba.aggregation;

import org.neo4j.procedure.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class ShortestAggregationFunction {
    public static final String name = "paths.intervals.shortest";

    @UserAggregationFunction(value = name)
    @Description("Returns the path with the shortest path.")
    public ShortestAggregator shortest()
    {
        return new ShortestAggregator();
    }

    public static class ShortestAggregator {

        private List<Object> pathList;
        private Map<String, Object> path;
        private List<Map<String, Object>> paths;

        private void changeList(List<Object> pathList, Map<String, Object> current) {
            this.pathList = pathList;
            this.paths = new LinkedList<>();
            this.paths.add(current);
        }

        @UserAggregationUpdate
        public void find(@Name("path") Map<String, Object> current) {
            if (current == null) {
                return;
            }
            List<Object> currentPathList = (List<Object>) current.get("path");
            if (this.pathList == null) {
                this.changeList(currentPathList, current);
                return;
            }

            if (pathList.size() > currentPathList.size()) {
                this.changeList(currentPathList, current);
            } else if (pathList.size() == currentPathList.size()) {
                this.paths.add(current);
            }
        }

        @UserAggregationResult
        public List<Map<String, Object>> result()
        {
            return this.paths;
        }

    }
}
