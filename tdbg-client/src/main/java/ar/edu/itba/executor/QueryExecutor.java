package ar.edu.itba.executor;

import ar.edu.itba.connector.Connector;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class QueryExecutor {

    private Connector connector = Connector.getInstance();
    private Session session;

    private static final Logger logger = LoggerFactory.getLogger(QueryExecutor.class);

    public List<Map<String, Object>> execute(String query) throws NoDatabaseConnectionException {
        logger.debug("Executing query");
        try {
            session = connector.getSession();
        } catch (NullPointerException e) {
            logger.error("Could not connect to database.");
            throw new NoDatabaseConnectionException();
        }
        StatementResult result = null;
        try {
            result = session.run(query);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        List<Map<String, Object>> resultList = result.list(record -> record.asMap(QueryExecutor::convert));
        logger.debug("Query executed");
        return resultList;
    }

    private static Object convert(Value value) {
        switch (value.type().name()) {
            case "PATH":
                return value.asList(QueryExecutor::convert);
            case "NODE":
            case "RELATIONSHIP":
                return value.asMap();
            case "NULL":
                return value.asString();
        }
        return value.asObject();
    }

    public void close(){
        connector.close();
    }
}
