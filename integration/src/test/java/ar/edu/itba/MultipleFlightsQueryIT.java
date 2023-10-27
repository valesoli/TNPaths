package ar.edu.itba;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MultipleFlightsQueryIT extends IntegrationTest {

    public MultipleFlightsQueryIT() {
        super("multiple_paths.cypher");
    }

    private String getQuery(final String procedure) {
        return "SELECT path\n" +
                "MATCH (c1:City)<-[:LocatedAt]-(a1:Airport),\n" +
                "(c2:City)<-[:LocatedAt]-(a2:Airport),\n" +
                "path = " + procedure + "((a1)-[:Flight*]->(a2))\n" +
                "WHERE c1.Name = 'City 1' AND c2.Name='City 2'";
    }

    private void assertPaths(StatementResult result) {
        List<Record> paths = result.list();
        assertThat(paths.size()).isEqualTo(6);

        assertThat(paths)
                .extracting((r) -> r.get("path").asMap())
                .flatExtracting("interval")
                .containsOnly("2000 — 2001");

        assertThat(paths)
                .extracting((r) -> r.get("path").asMap())
                .extracting((r) -> (List<Object>) r.get("path"))
                .extracting(List::size)
                .containsOnly(2);

        assertThat(paths)
                .extracting((r) -> r.get("path").asMap())
                .flatExtracting("path")
                .extracting("attributes")
                .flatExtracting("Name")
                .extracting("value")
                .containsOnly("Airport 1", "Airport 2", "Airport 3", "Airport 4", "Airport 5");
    }

    @Test
    public void testShortestPath() {
        String compiledQuery = compileQuery(this.getQuery("shortestPath"));
        runQuery(compiledQuery, this::assertPaths);
    }

    @Test
    public void testEarliestPath() {
        String compiledQuery = compileQuery(this.getQuery("earliestPath"));
        runQuery(compiledQuery, this::assertPaths);
    }

    @Test
    public void testFastestPath() {
        String compiledQuery = compileQuery(this.getQuery("fastestPath"));
        runQuery(compiledQuery, this::assertPaths);
    }

    @Test
    public void testLatestArrivalPath() {
        String compiledQuery = compileQuery(this.getQuery("latestArrivalPath"));
        runQuery(compiledQuery, this::assertPaths);
    }

    @Test
    public void testLatestDeparturePath() {
        String compiledQuery = compileQuery(this.getQuery("latestDeparturePath"));
        runQuery(compiledQuery, this::assertPaths);
    }
}
