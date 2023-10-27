package ar.edu.itba;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SocialNetworkQueryNowIT extends IntegrationTest {

    public SocialNetworkQueryNowIT() {
        super("social_network.cypher");
    }

    @Test
    public void testSnapshot() {
        String compiledQuery = compileQuery(
                "SELECT p2.Name as friend_name\n" +
                        "MATCH (p1:Person) - [:Friend] -> (p2:Person)\n" +
                        "WHERE p1.Name = 'Mary Smith-Taylor'\n" +
                        "SNAPSHOT 'NOW'");

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
                        "BETWEEN '2015' AND 'NOW'");

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
                        "BETWEEN '2018' AND 'NOW'");

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
}
