package ar.edu.itba;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SocialNetworkQueryYearIT extends IntegrationTest {

    public SocialNetworkQueryYearIT() {
        super("social_network.cypher");
    }

    @Test
    public void testPersons() {
        String compiledQuery = compileQuery("SELECT p.Name MATCH (p:Person)");
        runQuery(compiledQuery, (result) -> {
            assertThat(result.list())
                .extracting(r -> {
                    Value value = r.get("p.Name").get("value");
                    return value.asString();
                })
                .containsExactlyInAnyOrder(
                    "Cathy Van Bourne",
                    "Daniel Yang",
                    "Mary Smith",
                    "Mary Smith-Taylor",
                    "Pauline Boutler",
                    "Peter Burton",
                    "Sandra Carter"
                );
        });
    }

    @Test
    public void testPropertyAccess() {
        String compiledQuery = compileQuery("SELECT p[interval] as interval MATCH (p:Person) WHERE p.Name = 'Pauline Boutler'");
        runQuery(compiledQuery, (result) -> {
            assertThat(result.list())
                    .extracting(r -> {
                        List<String> values = new LinkedList<>();
                        r.get("interval").values().forEach(v -> values.add(v.asString()));
                        return String.join(",", values);
                    })
                    .containsExactlyInAnyOrder(
                            "1978—Now"
                    );
        });
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
                            "2015 — 2017",
                            "2010 — 2017"
                    );
        });
    }

    @Test
    public void testCPathOfLength3MissingEndpoint() {
        String compiledQuery = compileQuery(
                "SELECT p.path[0].attributes.Name as from, p.path[3].attributes.Name as to, p.interval as interval MATCH (n:Person), " +
                        "p=cPath((n)-[:Friend*3]->(:Person))");

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
                            "2015 — 2017",
                            "2010 — 2017"
                    );
        });
    }

    @Test
    public void testCPathOfLength3HeadAndLast() {
        String compiledQuery = compileQuery(
                "SELECT head(p.path).attributes.Name as from, last(p.path).attributes.Name as to MATCH (n:Person),(n2:Person), " +
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
        });
    }

    @Test
    public void testCPathOfLength3HeadAndLastWithId() {
        String compiledQuery = compileQuery(
                "SELECT head(p.path).attributes.Name as from, last(p.path).attributes.Name as to, p.interval as interval\n" +
                        "\tMATCH (n:Person), (m:Person), p = cPath((n) - [:Friend*3] -> (m))\n" +
                        "WHERE id(n) = 29 AND id(m) = 28\n");

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
                            "2010 — 2017"
                    );
        });
    }

    @Test
    public void testBooleanCPathOfLength3FromMary() {
        String compiledQuery = compileQuery(
                "SELECT n2.Name MATCH (n:Person),(n2:Person) " +
                        "WHERE n.Name = 'Mary Smith' AND  cPath((n)-[:Friend*3]->(n2))");

        runQuery(compiledQuery, (result) -> {
            List<Record> records = result.list();

            assertThat(records)
                    .extracting(r -> {
                        Value value = r.get("n2.Name").get("value");
                        return value.asString();
                    })
                    .containsExactlyInAnyOrder(
                            "Peter Burton"
                    );
        });
    }

    @Test
    public void testFriendsOfMaryWhenSheWasLivingInAntwerp() {
        String compiledQuery = compileQuery(
            "SELECT p2.Name as friend_name\n" +
            "MATCH (p1:Person) - [:Friend] -> (p2:Person)\n" +
            "WHERE p1.Name = 'Mary Smith-Taylor'\n" +
            "WHEN\n" +
                "MATCH (p1) - [e:LivedIn] -> (c:City)\n" +
                "WHERE c.Name = 'Antwerp'");

        runQuery(compiledQuery, (result) -> {
            List<Record> records = result.list();
            assertThat(records)
                    .extracting(r -> {
                        Value value = r.get("friend_name").get("value");
                        return value.asString();
                    })
                    .containsExactlyInAnyOrder(
                        "Pauline Boutler",
                        "Peter Burton"
                    );
        });
    }

    @Test
    public void testFollowSameBrands() {
        String compiledQuery = compileQuery(
            "SELECT c.Name as city_name, b1.Name as brand_name\n" +
            "MATCH (p1:Person) - [:LivedIn] -> (c:City),\n" +
            "(p1) - [:Fan] -> (b1:Brand)\n" +
            "WHERE p1.Name = 'Cathy Van Bourne'\n" +
            "WHEN\n" +
            "  MATCH (p2:Person) - [f:Fan] -> (b2:Brand)\n" +
            "  WHERE p2.Name = 'Sandra Carter' and b1.Name = b2.Name");

        runQuery(compiledQuery, (result) -> {
            List<Record> records = result.list();
            assertThat(records)
                    .extracting(r -> {
                        Value value = r.get("city_name").get("value");
                        return value.asString();
                    })
                    .containsExactlyInAnyOrder(
                        "Brussels"
                    );

            assertThat(records)
                    .extracting(r -> {
                        Value value = r.get("brand_name").get("value");
                        return value.asString();
                    })
                    .containsExactlyInAnyOrder(
                        "LG"
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
    public void testSelectMissingAttribute() {
        String compiledQuery = compileQuery(
                "SELECT p2.Name as friend_name, p2.Age as friend_age\n" +
                        "MATCH (p1:Person) - [:Friend] -> (p2:Person)\n" +
                        "WHERE p1.Name = 'Pauline Boutler'");

        runQuery(compiledQuery, (result) -> {
            List<Record> records = result.list();
            assertThat(records)
                    .extracting(r -> {
                        Value value = r.get("friend_name").get("value");
                        return value.asString();
                    })
                    .containsExactlyInAnyOrder(
                            "Sandra Carter",
                            "Cathy Van Bourne"
                    );
            assertThat(records)
                    .extracting(r -> {
                        return r.get("friend_age").isNull();
                    })
                    .containsExactlyInAnyOrder(
                            true,
                            true
                    );
        });
    }

    @Test
    public void testCPathOfLength3WithInterval() {
        String compiledQuery = compileQuery(
                "SELECT p.path[0].attributes.Name as from, p.path[3].attributes.Name as to, p.interval as interval\n" +
                        "MATCH (n:Person),(n2:Person),p = cPath((n)-[:Friend*3]->(n2), '2010', '2014')");

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
                            "2010 — 2017"
                    );
        });
    }

    private List<String> doNothing(Object r) {
        return (List<String>) r;
    }

    @Test
    public void testPairCPathOfLength3WithInterval() {
        String compiledQuery = compileQuery(
                "SELECT p.path[0].attributes.Name as from, p.path[3].attributes.Name as to, p.intervals as intervals\n" +
                        "MATCH (n:Person),(n2:Person),p = pairCPath((n)-[:Friend*3]->(n2), '2010', '2014')");

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
                        Value value = r.get("intervals");
                        return value.asList();
                    })
                    .flatExtracting(this::doNothing)
                    .containsExactlyInAnyOrder(
                            "2010 — 2017", "2002 — 2017"
                    );
        });
    }

    @Test
    public void testPairCPathOfLength3WithIntervalMissingEndpoint() {
        String compiledQuery = compileQuery(
                "SELECT p.path[0].attributes.Name as from, p.path[3].attributes.Name as to, p.intervals as intervals\n" +
                        "MATCH (n:Person),p = pairCPath((n)-[:Friend*3]->(:Person), '2010', '2014')");

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
                        Value value = r.get("intervals");
                        return value.asList();
                    })
                    .flatExtracting(this::doNothing)
                    .containsExactlyInAnyOrder(
                            "2010 — 2017", "2002 — 2017"
                    );
        });
    }

    @Test
    public void testPairCPathWithSumAndLength() {
        String compiledQuery = compileQuery(
                "SELECT sum(size(p.intervals)) as total_paths\n" +
                        "MATCH (n:Person),(n2:Person),p = pairCPath((n)-[:Friend*3]->(n2), '2010', '2014')");

        runQuery(compiledQuery, (result) -> {
            List<Record> records = result.list();
            assertThat(records.size()).isEqualTo(1);
            assertThat(records)
                .extracting(r -> r.get("total_paths").asInt())
                .containsExactly(1);
        });
    }
}
