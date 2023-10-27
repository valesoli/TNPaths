package ar.edu.itba;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FlightsQueryIT extends IntegrationTest {

    public FlightsQueryIT() {
        super("flights.cypher");
    }

    @Test
    public void testFlights() {
        String compiledQuery = compileQuery("SELECT a.Name MATCH (a:Airport)-[:LocatedAt]-(c:City) " +
                "WHERE c.Name = 'Buenos Aires'");
        runQuery(compiledQuery, (result) -> {
            assertThat(result.list())
                    .extracting(r -> {
                        Value value = r.get("a.Name").get("value");
                        return value.asString();
                    })
                    .containsExactlyInAnyOrder(
                        "AEP",
                        "EZE"
                    );
        });
    }

    private static List<Map<String, Object>> getPathFromListOfRecords(List<Record> paths, int index) {
        Record path = paths.get(index);
        Map<String, Object> pathMap = path.get("path").asMap();
        return (List<Map<String, Object>>) pathMap.get("path");
    }

    private static String getAttributeFromRecord(Map<String, Object> record, String attribute) {
        Map<String, List<Map<String, String>>> attrs = (Map<String, List<Map<String, String>>>) record.get("attributes");
        return attrs.get(attribute).get(0).get("value");
    }

    private static List<String> getInterval(List<Record> paths, int index) {
        Record path = paths.get(index);
        Map<String, Object> pathMap = path.get("path").asMap();
        return (List<String>) pathMap.get("interval");
    }

    @Test
    public void testFastestPath() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
                "(c2:City) <- [:LocatedAt] - (a2:Airport), \n" +
                "path = fastestPath((a1)-[:Flight*]->(a2))\n" +
                "WHERE c1.Name = 'London' AND c2.Name='Bariloche'");
        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(1);
            assertThat(getInterval(paths, 0))
                    .containsExactly("2020-03-07 23:15 — 2020-03-08 15:25");
            assertThat(getPathFromListOfRecords(paths, 0))
                    .extracting(r -> getAttributeFromRecord(r, "Name"))
                    .containsExactly(
                            "LGW", "EZE", "BRC"
                    );
        });
    }

    @Test
    public void testEarliestPath() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
                "(c2:City) <- [:LocatedAt] - (a2:Airport), \n" +
                "path = earliestPath((a1)-[:Flight*]->(a2))\n" +
                "WHERE c1.Name = 'London' AND c2.Name='Bariloche'");
        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(1);
            assertThat(getInterval(paths, 0))
                    .containsExactly("2020-03-07 21:00 — 2020-03-08 14:35");
            assertThat(getPathFromListOfRecords(paths, 0))
                    .extracting(r -> getAttributeFromRecord(r, "Name"))
                    .containsExactly(
                            "LHR", "GRU", "BRC"
                    );
        });
    }

    @Test
    public void testLatestDeparturePath() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City)<-[:LocatedAt]-(a1:Airport),\n" +
                "(c2:City)<-[:LocatedAt]-(a2:Airport), \n" +
                "path = latestDeparturePath((a1)-[:Flight*]->(a2), '2020-03-08 16:00')\n" +
                "WHERE c1.Name = 'London' AND c2.Name='Bariloche'");
        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(1);
            assertThat(getInterval(paths, 0))
                    .containsExactly("2020-03-07 23:15 — 2020-03-08 15:25");
            assertThat(getPathFromListOfRecords(paths, 0))
                    .extracting(r -> getAttributeFromRecord(r, "Name"))
                    .containsExactly(
                            "LGW", "EZE", "BRC"
                    );
        });
    }

    @Test
    public void testLatestArrivalPath() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City)<-[:LocatedAt]-(a1:Airport),\n" +
                "(c2:City)<-[:LocatedAt]-(a2:Airport), \n" +
                "path = latestArrivalPath((a1)-[:Flight*]->(a2), '2020-03-08 16:00')\n" +
                "WHERE c1.Name = 'London' AND c2.Name='Bariloche'");
        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(2);
            assertThat(getInterval(paths, 0))
                    .containsExactly("2020-03-07 23:15 — 2020-03-08 15:25");
            assertThat(getPathFromListOfRecords(paths, 0))
                    .extracting(r -> getAttributeFromRecord(r, "Name"))
                    .containsExactly(
                            "LGW", "EZE", "BRC"
                    );
            assertThat(getInterval(paths, 1))
                    .containsExactly("2020-03-07 22:30 — 2020-03-08 15:25");
            assertThat(getPathFromListOfRecords(paths, 1))
                    .extracting(r -> getAttributeFromRecord(r, "Name"))
                    .containsExactly(
                            "LHR", "EZE", "BRC"
                    );
        });
    }

    @Test
    public void testShortestPath() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City)<-[:LocatedAt]-(a1:Airport),\n" +
                "(c2:City)<-[:LocatedAt]-(a2:Airport), \n" +
                "path = shortestPath((a1)-[:Flight*]->(a2))\n" +
                "WHERE c1.Name = 'London' AND c2.Name='Bariloche'");
        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(3);

            assertThat(getInterval(paths, 0))
                            .containsExactly("2020-03-07 23:15 — 2020-03-08 15:25");
            assertThat(getPathFromListOfRecords(paths, 0))
                    .extracting(r -> getAttributeFromRecord(r, "Name"))
                    .containsExactly(
                            "LGW", "EZE", "BRC"
                    );
            assertThat(getPathFromListOfRecords(paths, 0).size())
                    .isEqualTo(3);

            assertThat(getInterval(paths, 1))
                    .containsExactly("2020-03-07 22:30 — 2020-03-08 15:25");
            assertThat(getPathFromListOfRecords(paths, 1))
                    .extracting(r -> getAttributeFromRecord(r, "Name"))
                    .containsExactly(
                            "LHR", "EZE", "BRC"
                    );
            assertThat(getPathFromListOfRecords(paths, 2).size())
                    .isEqualTo(3);

            assertThat(getInterval(paths, 2))
                    .containsExactly("2020-03-07 21:00 — 2020-03-08 14:35");
            assertThat(getPathFromListOfRecords(paths, 2))
                    .extracting(r -> getAttributeFromRecord(r, "Name"))
                    .containsExactly(
                            "LHR", "GRU", "BRC"
                    );
            assertThat(getPathFromListOfRecords(paths, 1).size())
                    .isEqualTo(3);

        });
    }
}
