package ar.edu.itba.algorithms.strategies.paths;

import org.neo4j.logging.Log;

public class BooleanNodesIntersectionPathsStrategy extends NodesIntersectionPathsStrategy {

    public BooleanNodesIntersectionPathsStrategy(Long minimumLength, Long maximumLength, Log log) {
        super(minimumLength, maximumLength, log);
    }

    @Override
    public boolean isFinished() {
        return super.isFinished() || !getSolutionPaths().isEmpty();
    }
}
