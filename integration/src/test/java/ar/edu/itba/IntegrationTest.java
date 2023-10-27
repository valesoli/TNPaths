package ar.edu.itba;

import ar.edu.itba.aggregation.*;
import ar.edu.itba.grammar.QueryCompiler;
import ar.edu.itba.procedures.EmbeddedNeo4jTest;
import ar.edu.itba.procedures.consecutive.*;
import ar.edu.itba.procedures.cotemporal.CoTemporalPathsWithNodes;
import ar.edu.itba.procedures.cotemporal.CoTemporalPathsWithPath;
import ar.edu.itba.procedures.temporal.BetweenProcedure;
import ar.edu.itba.procedures.temporal.SnapshotProcedure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.harness.TestServerBuilders;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTest extends EmbeddedNeo4jTest {

    private String fixture;

    public IntegrationTest(String fixture) {
        this.fixture = fixture;
    }

    @BeforeAll
    void initializeNeo4j() {
        this.setEmbeddedDatabaseServer(
                buildServer(TestServerBuilders
                        .newInProcessBuilder()
                        .withProcedure(BetweenProcedure.class)
                        .withProcedure(SnapshotProcedure.class)
                        .withProcedure(CoTemporalPathsWithPath.class)
                        .withProcedure(CoTemporalPathsWithNodes.class)
                        .withProcedure(LatestDeparturePathProcedure.class)
                        .withProcedure(EarliestArrivalPathProcedure.class)
                        .withProcedure(FastestPathProcedure.class)
                        .withProcedure(ShortestPathProcedure.class)
                        .withProcedure(LatestArrivalPathProcedure.class)
                        .withAggregationFunction(EarliestAggregationFunction.class)
                        .withAggregationFunction(FastestAggregationFunction.class)
                        .withAggregationFunction(LatestDepartureAggregationFunction.class)
                        .withAggregationFunction(LatestArrivalAggregationFunction.class)
                        .withAggregationFunction(ShortestAggregationFunction.class)
                )
        );
    }

    public String compileQuery(String query) {
        return new QueryCompiler().compile(query);
    }

    @Override
    public String getFixture() {
        return this.fixture;
    }
}
