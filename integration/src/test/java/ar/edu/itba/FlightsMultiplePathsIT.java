package ar.edu.itba;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Record;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FlightsMultiplePathsIT extends IntegrationTest {

    public FlightsMultiplePathsIT() {
        super("flights-multiple-paths.cypher");
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

    private static void assertIntervalAndPath(
            List<Record> paths, int index, String interval, String[] path
    ) {
        assertThat(getInterval(paths, index))
                .contains(interval);
        assertThat(getPathFromListOfRecords(paths, index))
                .extracting(r -> getAttributeFromRecord(r, "Name"))
                .containsExactly(path);
    }

    @Test
    public void testShortestPathBarilocheToLondon() {
        String compiledQuery = compileQuery("SELECT path\n" +
            "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
            "(c2:City) <- [:LocatedAt] - (a2:Airport),\n" +
            "path = shortestPath((a1)-[:Flight*]->(a2))\n" +
            "WHERE c1.Name = 'Bariloche' AND c2.Name='London'");

        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(4);
            assertIntervalAndPath(
                    paths,
                    0,
                    "2020-03-07 09:30 — 2020-03-07 21:30",
                    new String[]{"BRC", "EZE", "LGW"}
            );
            assertIntervalAndPath(
                    paths,
                    2,
                    "2020-03-07 17:00 — 2020-03-08 06:55",
                    new String[]{"BRC", "GRU", "LHR"}
            );
            assertIntervalAndPath(
                    paths,
                    1,
                    "2020-03-07 09:30 — 2020-03-07 21:40",
                    new String[]{"BRC", "EZE", "LHR"}
            );
            assertIntervalAndPath(
                    paths,
                    1,
                    "2020-03-07 09:30 — 2020-03-08 21:40",
                    new String[]{"BRC", "EZE", "LHR"}
            );
            assertIntervalAndPath(
                    paths,
                    3,
                    "2020-03-07 21:00 — 2020-03-08 15:00",
                    new String[]{"BRC", "AEP", "STN"}
            );
        });
    }

    @Test
    public void testFastestPathBarilocheToLondon() {
        String compiledQuery = compileQuery("SELECT path\n" +
            "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
            "(c2:City) <- [:LocatedAt] - (a2:Airport),\n" +
            "path = fastestPath((a1)-[:Flight*]->(a2))\n" +
            "WHERE c1.Name = 'Bariloche' AND c2.Name='London'");

        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(1);
            assertIntervalAndPath(
                    paths,
                    0,
                    "2020-03-07 09:30 — 2020-03-07 21:00",
                    new String[]{"BRC", "EZE", "LIM", "LGW"}
            );
        });
    }

    @Test
    public void testEarliestPathBarilocheToLondon() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
                "(c2:City) <- [:LocatedAt] - (a2:Airport),\n" +
                "path = earliestPath((a1)-[:Flight*]->(a2))\n" +
                "WHERE c1.Name = 'Bariloche' AND c2.Name='London'");

        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(1);
            assertIntervalAndPath(
                    paths,
                    0,
                    "2020-03-07 09:30 — 2020-03-07 21:00",
                    new String[]{"BRC", "EZE", "LIM", "LGW"}
            );
        });
    }

    @Test
    public void testDeparturePathBarilocheToLondon() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
                "(c2:City) <- [:LocatedAt] - (a2:Airport),\n" +
                "path = latestDeparturePath((a1)-[:Flight*]->(a2),'2020-03-08 16:00')\n" +
                "WHERE c1.Name = 'Bariloche' AND c2.Name='London'");

        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(1);
            assertIntervalAndPath(
                    paths,
                    0,
                    "2020-03-07 21:00 — 2020-03-08 15:00",
                    new String[]{"BRC", "AEP", "STN"}
            );
        });
    }

    @Test
    public void testDeparturePath2BarilocheToLondon() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
                "(c2:City) <- [:LocatedAt] - (a2:Airport),\n" +
                "path = latestDeparturePath((a1)-[:Flight*]->(a2),'2020-03-07 23:30')\n" +
                "WHERE c1.Name = 'Bariloche' AND c2.Name='London'");

        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(3);
            assertThat(paths.size()).isEqualTo(3);
            assertIntervalAndPath(
                    paths,
                    1,
                    "2020-03-07 09:30 — 2020-03-07 21:00",
                    new String[]{"BRC", "EZE", "LIM", "LGW"}
            );
            assertIntervalAndPath(
                    paths,
                    0,
                    "2020-03-07 09:30 — 2020-03-07 21:30",
                    new String[]{"BRC", "EZE", "LGW"}
            );
            assertThat(paths.size()).isEqualTo(3);
            assertIntervalAndPath(
                    paths,
                    2,
                    "2020-03-07 09:30 — 2020-03-07 21:40",
                    new String[]{"BRC", "EZE", "LHR"}
            );
        });
    }

    @Test
    public void testShortestPathLondonToBariloche() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
                "(c2:City) <- [:LocatedAt] - (a2:Airport),\n" +
                "path = shortestPath((a1)-[:Flight*]->(a2))\n" +
                "WHERE c1.Name = 'London' AND c2.Name='Bariloche'");

        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(4);
            assertIntervalAndPath(
                    paths,
                    0,
                    "2020-03-07 23:15 — 2020-03-08 15:25",
                    new String[]{"LGW", "EZE", "BRC"}
            );

            assertIntervalAndPath(
                    paths,
                    1,
                    "2020-03-07 22:30 — 2020-03-08 15:25",
                    new String[]{"LHR", "EZE", "BRC"}
            );

            assertIntervalAndPath(
                    paths,
                    2,
                    "2020-03-07 21:00 — 2020-03-08 14:35",
                    new String[]{"LHR", "GRU", "BRC"}
            );

            assertIntervalAndPath(
                    paths,
                    3,
                    "2020-03-07 22:40 — 2020-03-08 14:35",
                    new String[]{"STN", "GRU", "BRC"}
            );
        });
    }

    @Test
    public void testFastestPathLondonToBariloche() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
                "(c2:City) <- [:LocatedAt] - (a2:Airport),\n" +
                "path = fastestPath((a1)-[:Flight*]->(a2))\n" +
                "WHERE c1.Name = 'London' AND c2.Name='Bariloche'");

        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(1);
            assertIntervalAndPath(
                    paths,
                    0,
                    "2020-03-07 23:35 — 2020-03-08 15:25",
                    new String[]{"LGW", "LIM", "EZE", "BRC"}
            );
        });
    }

    @Test
    public void testEarliestPathLondonToBariloche() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
                "(c2:City) <- [:LocatedAt] - (a2:Airport),\n" +
                "path = earliestPath((a1)-[:Flight*]->(a2))\n" +
                "WHERE c1.Name = 'London' AND c2.Name='Bariloche'");

        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(2);
            assertIntervalAndPath(
                    paths,
                    0,
                    "2020-03-07 21:00 — 2020-03-08 14:35",
                    new String[]{"LHR", "GRU", "BRC"}
            );
            assertIntervalAndPath(
                    paths,
                    1,
                    "2020-03-07 22:40 — 2020-03-08 14:35",
                    new String[]{"STN", "GRU", "BRC"}
            );
        });
    }

    @Test
    public void testLatestDeparturePathLondonToBariloche() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
                "(c2:City) <- [:LocatedAt] - (a2:Airport), \n" +
                "path = latestDeparturePath((a1)-[:Flight*]->(a2),'2020-03-08 16:00')\n" +
                "WHERE c1.Name = 'London' AND c2.Name='Bariloche'");

        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(1);
            assertIntervalAndPath(
                    paths,
                    0,
                    "2020-03-07 23:35 — 2020-03-08 15:25",
                    new String[]{"LGW", "LIM", "EZE", "BRC"}
            );
        });
    }

    @Test
    public void testLatestDeparturePath2LondonToBariloche() {
        String compiledQuery = compileQuery("SELECT path\n" +
                "MATCH (c1:City) <- [:LocatedAt] - (a1:Airport),\n" +
                "(c2:City) <- [:LocatedAt] - (a2:Airport), \n" +
                "path = latestDeparturePath((a1)-[:Flight*]->(a2),'2020-03-08 15:00')\n" +
                "WHERE c1.Name = 'London' AND c2.Name='Bariloche'");

        runQuery(compiledQuery, (result) -> {
            List<Record> paths = result.list();

            assertThat(paths.size()).isEqualTo(1);
            assertIntervalAndPath(
                    paths,
                    0,
                    "2020-03-07 22:40 — 2020-03-08 14:35",
                    new String[]{"STN", "GRU", "BRC"}
            );
        });
    }
}
