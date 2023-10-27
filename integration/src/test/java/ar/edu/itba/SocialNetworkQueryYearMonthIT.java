package ar.edu.itba;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SocialNetworkQueryYearMonthIT extends IntegrationTest {

    public SocialNetworkQueryYearMonthIT() {
        super("year_month.cypher");
    }

    @Test
    public void testCPathOfLength3() {
        String compiledQuery = compileQuery(
                "SELECT p.path[0].attributes.Name as from, p.path[3].attributes.Name as to, p.interval as interval MATCH (n:Person),(n2:Person), " +
                        "p=cPath((n)-[:Friend*3]->(n2))");

        runQuery(compiledQuery, (result) -> {
            List<Record> records = result.list();
            assertThat(records)
                    .extracting(r -> {
                        List<String> values = new LinkedList<>();
                        r.get("from").values().forEach(v -> values.add(v.get("value").asString()));
                        return String.join(",", values);
                    })
                    .containsExactlyInAnyOrder(
                            "Mary Smith-Taylor",
                            "Pauline Boutler"
                    );

            assertThat(records)
                    .extracting(r -> {
                        List<String> values = new LinkedList<>();
                        r.get("to").values().forEach(v -> values.add(v.get("value").asString()));
                        return String.join(",", values);
                    })
                    .containsExactlyInAnyOrder(
                            "Peter Burton",
                            "Daniel Yang"
                    );
            assertThat(records)
                    .flatExtracting(r -> {
                        Value value = r.get("interval");
                        return value.asList();
                    })
                    .containsExactlyInAnyOrder(
                            "2015-01 — 2017-12",
                            "2010-01 — 2017-12"
                    );
        });
    }

    @Test
    public void testSnapshot() {
        String compiledQuery = compileQuery(
                "SELECT p2.Name as friend_name\n" +
                        "MATCH (p1:Person) - [:Friend] -> (p2:Person)\n" +
                        "WHERE p1.Name = 'Mary Smith-Taylor'\n" +
                        "SNAPSHOT '1995'");

        runQuery(compiledQuery, (result) -> {
            List<Record> records = result.list();
            assertThat(records)
                    .extracting(r -> {
                        Value value = r.get("friend_name").get("value");
                        return value.asString();
                    })
                    .containsExactlyInAnyOrder(
                            "Peter Burton"
                    );
        });
    }

    @Test
    public void testBetween() {
        String compiledQuery = compileQuery(
                "SELECT p2.Name as friend_name\n" +
                        "MATCH (p1:Person) - [f:Friend] -> (p2:Person)\n" +
                        "WHERE p1.Name = 'Cathy Van Bourne'\n" +
                        "BETWEEN '2015' AND '2018'");

        runQuery(compiledQuery, (result) -> {
            List<Record> records = result.list();
            assertThat(records)
                    .extracting(r -> {
                        Value value = r.get("friend_name").get("value");
                        return value.asString();
                    })
                    .containsExactlyInAnyOrder(
                            "Peter Burton"
                    );
        });
    }

    @Test
    public void testBetweenWithoutVariableInEdge() {
        String compiledQuery = compileQuery(
                "SELECT p2.Name as friend_name\n" +
                        "MATCH (p1:Person) - [:Friend] -> (p2:Person)\n" +
                        "WHERE p1.Name = 'Pauline Boutler'\n" +
                        "BETWEEN '2018' AND '2020'");

        runQuery(compiledQuery, (result) -> {
            List<Record> records = result.list();
            assertThat(records)
                    .extracting(r -> {
                        Value value = r.get("friend_name").get("value");
                        return value.asString();
                    })
                    .containsExactlyInAnyOrder(
                            "Sandra Carter"
                    );
        });
    }

    @Test
    public void testCPathOfLength3WithInterval() {
        String compiledQuery = compileQuery(
                "SELECT p.path[0].attributes.Name as from, p.path[3].attributes.Name as to, p.interval as interval\n" +
                        "MATCH (n:Person),(n2:Person),p = cPath((n)-[:Friend*3]->(n2), '2010-03', '2014-03')");

        runQuery(compiledQuery, (result) -> {
            List<Record> records = result.list();
            assertThat(records)
                    .extracting(r -> {
                        List<String> values = new LinkedList<>();
                        r.get("from").values().forEach(v -> values.add(v.get("value").asString()));
                        return String.join(",", values);
                    })
                    .containsExactlyInAnyOrder(
                            "Mary Smith-Taylor"
                    );

            assertThat(records)
                    .extracting(r -> {
                        List<String> values = new LinkedList<>();
                        r.get("to").values().forEach(v -> values.add(v.get("value").asString()));
                        return String.join(",", values);
                    })
                    .containsExactlyInAnyOrder(
                            "Peter Burton"
                    );
            assertThat(records)
                    .flatExtracting(r -> {
                        Value value = r.get("interval");
                        return value.asList();
                    })
                    .containsExactlyInAnyOrder(
                            "2010-01 — 2017-12"
                    );
        });
    }
}
