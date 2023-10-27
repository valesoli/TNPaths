package ar.edu.itba.grammar;

import ar.edu.itba.grammar.struct.*;
import ar.edu.itba.grammar.struct.params.FunctionParams;
import ar.edu.itba.grammar.struct.params.TemporalFilterParams;
import ar.edu.itba.util.Pair;
import kotlin.jvm.functions.FunctionN;

import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.*;
import java.util.stream.Collectors;

public class CypherOutputGenerator {
    private static final String MATCH = "MATCH";
    private static final String OPTIONAL_MATCH = "OPTIONAL MATCH";
    private static final String WHERE = "WHERE";
    private static final String RETURN = "RETURN";
    public static final String UNWIND = "UNWIND";

    private static final String ATTRIBUTE = "Attribute";
    private static final String OBJECT = "Object";
    public static final String VALUE = "Value";
    private static final String TITLE = "title";
    public static final String AS = "as";
    public static final String BOOL = "bool";
    public static final String INTERNAL = "internal";

    private static final String CALL = "CALL";
    private static final String YIELD = "YIELD";
    private static final String PATH = "path";
    public static final String INTERVAL = "interval";
    public static final String INTERVALS = "intervals";
    public static final String WITH = "WITH";
    private static final String DISTINCT = "DISTINCT";
    private static final String ROW = "row";
    private static final String ALPHAS = "alphas";
    

    public static final String FUNCTION_EARLIEST = "earliestpath";
    public static final String FUNCTION_FASTEST = "fastestpath";
    public static final String FUNCTION_SHORTEST = "shortestpath";
    public static final String FUNCTION_LATEST_ARRIVAL = "latestarrivalpath";
    public static final String FUNCTION_LATEST_DEPARTURE = "latestdeparturepath";
    public static final String FUNCTION_CPATH = "cpath";
    public static final String FUNCTION_SENSOR_FLOWING = "fpath";
    public static final String FUNCTION_SENSOR_FLOWING_BACKWARDS = "fbpath";
    public static final String FUNCTION_SENSOR_CPATH = "rscpath";
    public static final String FUNCTION_CPATH2 = "cpath2";
    public static final String FUNCTION_CPATH_EXISTS = FUNCTION_CPATH + BOOL;
    public static final String FUNCTION_PAIR_CPATH = "paircpath";
    public static final String FUNCTION_PAIR_CPATH_EXISTS = FUNCTION_PAIR_CPATH + BOOL;
    public static final String FUNCTION_ALPHA = "alphapath";


    private static final String TEMPORAL = "temporal";
    private static final String PROCEDURE_SNAPSHOT = TEMPORAL + CharConstants.POINT + "snapshot";
    private static final String PROCEDURE_BETWEEN = TEMPORAL + CharConstants.POINT + "between";

    private static final String CONSECUTIVE = "consecutive";
    private static final String ALPHA = "alpha";
    public static final String PROCEDURE_EARLIEST = CONSECUTIVE + CharConstants.POINT + "earliest";
    public static final String PROCEDURE_FASTEST = CONSECUTIVE + CharConstants.POINT + "fastest";
    public static final String PROCEDURE_SHORTEST = CONSECUTIVE + CharConstants.POINT + "shortest";
    public static final String PROCEDURE_LATEST_DEPARTURE = CONSECUTIVE + CharConstants.POINT + "latestDeparture";
    public static final String PROCEDURE_LATEST_ARRIVAL = CONSECUTIVE + CharConstants.POINT + "latestArrival";
    public static final String PROCEDURE_SENSOR_FLOWING = CONSECUTIVE + CharConstants.POINT + "sensorFlowing";
    public static final String PROCEDURE_SENSOR_FLOWING_BACKWARDS = CONSECUTIVE + CharConstants.POINT + "sensorFlowingBackwards";
    public static final String PROCEDURE_ALPHA = ALPHA + CharConstants.POINT + "alphaPath";

    private static final String COEXISTING = "coexisting";
    private static final String COTEMPORAL_PATHS = "coTemporalPaths";
    private static final String COTEMPORAL_PATHS_SOURCE_ONLY = "sourceOnlyCoTemporalPaths";
    private static final String COTEMPORAL_PATHS_NODES = "coTemporalPathsNodes";
    private static final String COTEMPORAL_PATHS_ATTRIBUTES = "coTemporalAttributePaths";
    private static final String EXISTS = "exists";
    private static final String PROCEDURE_CPATH = COEXISTING + CharConstants.POINT + COTEMPORAL_PATHS;
    private static final String PROCEDURE_CPATH2 = COEXISTING + CharConstants.POINT + COTEMPORAL_PATHS + '2';
    private static final String PROCEDURE_CPATH_EXISTS = COEXISTING + CharConstants.POINT + COTEMPORAL_PATHS + CharConstants.POINT + EXISTS;
    private static final String PROCEDURE_PAIR_CPATH = COEXISTING + CharConstants.POINT + COTEMPORAL_PATHS_NODES;
    private static final String PROCEDURE_PAIR_CPATH_EXISTS = COEXISTING + CharConstants.POINT + COTEMPORAL_PATHS_NODES + CharConstants.POINT + EXISTS;
    private static final String PROCEDURE_SENSOR_CPATH = COEXISTING + CharConstants.POINT + COTEMPORAL_PATHS_ATTRIBUTES;
    
    private static final String GRAPHINDEX = "graphindex";
    private static final String GRAPH_CREATE_OR_UPDATE = "concatInsertOrUpdateEdge";
    private static final String TREE_CREATE_OR_UPDATE = "treeInsertOrUpdateEdge";
    private static final String GRAPH_DELETE = "concatDeleteEdge";
    private static final String TREE_DELETE = "treeDeleteEdge";
    private static final String RETRIEVE_PATHS = "quickRetrievePathsConcat";
    private static final String RETRIEVE_PATHS_SOURCE_ONLY = "sourceOnlyRetrievePathsConcat";
    private static final String RETRIEVE_PATHS_EXISTS = "coTemporalPathsExist";
    private static final String CREATE_TREE_INDEX = "createTreeIndex";
    private static final String CREATE_INDEX = "createIndex";
    private static final String CONNECT_INDEX = "connectIndex";
    private static final String INDEX_CONDITION = "index_condition";

    private static final String APOC_WHEN = "apoc.when";

    private static final String FULL_RIGHT = "-->";
    public static final String AND = CharConstants.SPACE + "AND" + CharConstants.SPACE;

    private static final List<Integer> TEMPORAL_OP_PRETTY_PRINTING = Arrays.asList(0, 1, 2);

    private static final int INDEX_OBJECTS_PER_BLOCK = 500;
    private static final int INDEX_CONNECTIONS_PER_BLOCK = 50000;

    private PreservedStateHelper preservedStateHelper = PreservedStateHelper.STATE;

    private boolean matchCommaAtBeginning = false;

    private StringBuilder match = new StringBuilder();
    private StringBuilder where = new StringBuilder();
    private StringBuilder functionWhere = new StringBuilder();
    private StringBuilder optionalMatch = new StringBuilder();
    private StringBuilder call = new StringBuilder();
    private StringBuilder ret = new StringBuilder();
    private StringBuilder whenWhere = new StringBuilder();
    private StringBuilder whenMatch = new StringBuilder();
    private StringBuilder limit = new StringBuilder();
    private StringBuilder skip = new StringBuilder();

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private QueryState state = QueryState.MATCH;
    private FunctionType functionType = null;

    /**
     * Stores the paths found in match
     */
    private List<Path> matchPaths = new LinkedList<>();

    /**
     * Name of the edge for the when interval
     */
    private String whenEdge;

    private ParserVariables parserVariablesGenerator;

    private final Map<String, Variable> variableAttributes;

    private CacheableContent cacheableContent;

    private boolean isIndexEnabled = false;

    public void setIsIndexEnabled(boolean isEnabled) {
        preservedStateHelper.setIsIndexEnabled(isEnabled);
    }

    public void setToggleQuery() {
        String toggleType = preservedStateHelper.getIsIndexEnabled() ? "ENABLE_INDEX" : "DISABLE_INDEX";
        this.match = new StringBuilder(toggleType);
        this.ret = new StringBuilder("");
    }

    public CypherOutputGenerator(ParserVariables parserVariablesGenerator,
                                    final Map<String, Variable> variableAttributes,
                                    final CacheableContent cacheableContent) {
        this.parserVariablesGenerator = parserVariablesGenerator;
        this.variableAttributes = variableAttributes;
        this.cacheableContent = cacheableContent;

        match
            .append(MATCH)
            .append(CharConstants.SPACE);
        ret
            .append(RETURN);
    }


    public FunctionType getFunctionType() {
        return functionType;
    }


    public void setFunctionType(FunctionType functionType) {
        this.functionType = functionType;
    }

    public void unsetFunctionType() {
        this.functionType = null;
    }

    public void retAppend(final String s) {
        if (!ret.toString().equals(RETURN))
            ret.append(CharConstants.COMMA);
        ret.append(CharConstants.SPACE);
        ret.append(s);
    }

    public void whereAppend(final String s) {
        where.append(s);
    }

    public void matchAppend(final String s) {
        match.append(s);
    }

    /**
     * Used to append the path generated by an attribute access in SELECT or WHERE to match clause
     * @param definition node attribute value path
     * @param foundInSelect if the value is found in SELECT clause or not
     */
    public void matchAppendDefinition(final String definition, final boolean foundInSelect) {
        final String match = foundInSelect ? OPTIONAL_MATCH : MATCH;
        if (foundInSelect) {
            this.optionalMatch.append(match)
                    .append(CharConstants.SPACE)
                    .append(definition)
                    .append(CharConstants.LINE_SEPARATOR);
        }
        else {
            this.match.append(match)
                    .append(CharConstants.SPACE)
                    .append(definition)
                    .append(CharConstants.LINE_SEPARATOR);
        }
    }

    /**
     * Used to append a path
     * @param definition node attribute value path
     */
    public void whenAppendDefinition(final String definition) {
        whenMatch.append(MATCH)
                 .append(CharConstants.SPACE)
                 .append(definition)
                 .append(CharConstants.LINE_SEPARATOR);
    }

    /**
     * Appends an object definition to match or when match
     * @param variable variable
     * @param objectTitle user label that is used for the object node title
     */
    public String matchAppendObjectDef(final Optional<String> variable, final String objectTitle) {
        String objectDef = generateNode(variable, OBJECT, Optional.of(objectTitle));

        switch (this.state) {
            case MATCH:
                match.append(objectDef);
                break;
            case WHEN:
                whenMatch.append(objectDef);
                break;
        }
        return objectDef;
    }

    public void matchAppendComma() {
        if (matchCommaAtBeginning)
            switch (state) {
                case MATCH:
                    match.append(CharConstants.COMMA);
                    break;
                case WHEN:
                    whenMatch.append(CharConstants.COMMA);
                    break;
            }
    }

    public void setMatchCommaAtBeginning(final boolean value) {
        matchCommaAtBeginning = value;
    }

    public void appendCommaForPaths() {
        switch (state) {
            case MATCH:
                this.matchAppendComma();
                this.setMatchCommaAtBeginning(true);
                break;
        }
    }

    /**
     * Generates the path variable -> attribute -> value
     * @param variable variable of the object
     * @param attribute attribute node to be expanded
     * @param value alias of the value node
     * @return a path
     */
    public String createPath(final String variable, final String attribute, final String value) {
        return generatePath(variable, attribute, value);
    }

    private String generatePath(final String objectVariable, final String attributeName, final String valueAlias) {
        return new StringBuilder()
                .append(CharConstants.OPENING_PARENTHESIS)
                .append(objectVariable)
                .append(CharConstants.CLOSING_PARENTHESIS)
                .append(FULL_RIGHT)
                .append(generateAttributeNode(attributeName))
                .append(FULL_RIGHT)
                .append(generateValueNode(valueAlias))
                .toString();
    }

    private String generateAttributeNode(final String attributeName) {
        final String variable = parserVariablesGenerator.createVariable(attributeName);
        return generateNode(Optional.of(variable), ATTRIBUTE, Optional.of(attributeName));
    }

    private String generateValueNode(final String variable) {
        return generateNode(Optional.of(variable), VALUE, Optional.empty());
    }

    /**
     * Generates a generic cypher node
     * @param variable alias of the node
     * @param label label of the node
     * @param title title of the node. Value nodes don´t have a title
     * @return String cypher node definition
     */
    private String generateNode(final Optional<String> variable, final String label, final Optional<String> title) {
       return new StringBuilder()
               .append(CharConstants.OPENING_PARENTHESIS)
               .append(variable.orElse(""))
               .append(CharConstants.COLON)
               .append(label)
               .append(title.map(this::generateTitle).orElse(""))
               .append(CharConstants.CLOSING_PARENTHESIS)
               .toString();
    }

    /**
     * Creates the title field filter for a node definition
     * @param title title of node
     * @return String cypher title definition
     */
    private String generateTitle(final String title) {
        String ans = new StringBuilder()
                .append(CharConstants.SPACE)
                .append(CharConstants.OPENING_BRACKET)
                .append(TITLE)
                .append(CharConstants.COLON)
                .append(CharConstants.SPACE)
                .append(CharConstants.QUOTE)
                .append(title)
                .append(CharConstants.QUOTE)
                .append(CharConstants.CLOSING_BRACKET)
                .toString();
        return ans;
    }

    /**
     * Creates a query without temporal filters
     * @see TemporalFilterParams
     * @param functionParams function call parameters if any
     * @return String cypher query
     */
    public String generateNonTemporalFilterQuery(final List<FunctionParams> functionParams) {
        final StringBuilder output = new StringBuilder()
                .append(match);

        if (where.length() > 0)
            output.append(WHERE)
                  .append(CharConstants.SPACE)
                  .append(where)
                  .append(CharConstants.LINE_SEPARATOR);

        output.append(optionalMatch);

        if (!functionParams.isEmpty()) {
            generateFunctionCalls(functionParams);
            output.append(call);
        }

        if (functionWhere.length() > 0) {
            output.append(WHERE)
                  .append(CharConstants.SPACE)
                  .append(functionWhere)
                  .append(CharConstants.LINE_SEPARATOR);
        }

        output.append(ret);

        if (skip.length() > 0) {
            output.append(CharConstants.LINE_SEPARATOR)
                  .append(skip);
        }

        if (limit.length() > 0) {
            output.append(CharConstants.LINE_SEPARATOR)
                  .append(limit);
        }

        return output.toString();
    }

    public String generateSnapshotQuery(final TemporalFilterParams snapshotParams) {
        final ReturnEntitiesJson returnEntitiesJson = new ReturnEntitiesJson(snapshotParams.getReturnEntities());
        return generateTemporalFilterCall(PROCEDURE_SNAPSHOT, snapshotParams.getReturnEntities(), TEMPORAL_OP_PRETTY_PRINTING, new FunctionArgument(escapeQuery(snapshotParams.getQuery())),
                new FunctionArgument(snapshotParams.getValue()), new FunctionArgument(jsonWithoutQuotesInKeys(returnEntitiesJson), false));
    }

    public String generateBetweenQuery(final TemporalFilterParams betweenParams) {
        final ReturnEntitiesJson returnEntitiesJson = new ReturnEntitiesJson(betweenParams.getReturnEntities());
        return generateTemporalFilterCall(PROCEDURE_BETWEEN,  betweenParams.getReturnEntities(), TEMPORAL_OP_PRETTY_PRINTING, new FunctionArgument(escapeQuery(betweenParams.getQuery())),
                new FunctionArgument(betweenParams.getValue()), new FunctionArgument(jsonWithoutQuotesInKeys(returnEntitiesJson), false));
    }

    /**
     * Creates json without quotes in keys
     * Example: { ret: { key: "value" } }
     * @param returnEntitiesJson entities to return
     * @return String representation of json
     */
    private String jsonWithoutQuotesInKeys(ReturnEntitiesJson returnEntitiesJson) {
        String json = gson.toJson(returnEntitiesJson);
        return json.replaceAll("\"([^\"]+)\":", "$1:");
    }

    /**
     * Escapes quotes in query
     * @param query query string
     * @return query with escaped quotes
     */
    private String escapeQuery(final String query) {
        return query.replace(CharConstants.QUOTE.toString(),
                CharConstants.BACKSLASH.toString() + CharConstants.QUOTE);
    }

    public String generateTemporalInnerQuery() {
        StringBuilder query = new StringBuilder()
                .append(match);

        if (where.length() > 0)
            query
                .append(WHERE)
                .append(CharConstants.SPACE)
                .append(where)
                .append(CharConstants.LINE_SEPARATOR);

        query.append(optionalMatch);

        if (call.length() > 0)
            query
                .append(call)
                .append(CharConstants.LINE_SEPARATOR);

        query.append(RETURN)
            .append(CharConstants.SPACE)
            .append(CharConstants.ASTERISK);

        return query.toString();
    }

    /**
     * Generates a CALL to a temporal procedure
     * @param name name of the procedure to be called
     * @param lineSeparatorPosition pretty printing
     * @param args arguments of the procedure
     * @return a query with a CALL
     */
    public String generateTemporalFilterCall(final String name, final List<ReturnEntity> returnEntities,
                                    final List<Integer> lineSeparatorPosition, final FunctionArgument... args) {
        final StringBuilder output = new StringBuilder();
        output
            .append(CALL)
            .append(CharConstants.SPACE)
            .append(name)
            .append(CharConstants.OPENING_PARENTHESIS);

        for (int i = 0 ; i < args.length ; i++) {
            if (lineSeparatorPosition.contains(i))
                output.append(CharConstants.LINE_SEPARATOR);
            else
                output.append(CharConstants.SPACE);

            FunctionArgument argument = args[i];
            String arg = argument.getValue();

            if (argument.isString())
                output.append(CharConstants.QUOTE);

            output.append(arg);

            if (argument.isString())
                output.append(CharConstants.QUOTE);

            if (i != args.length - 1) {
                output.append(CharConstants.COMMA);
            }
        }
        output.append(CharConstants.CLOSING_PARENTHESIS);
        output.append(generateTemporalFilterReturn(returnEntities));

        if (skip.length() > 0) {
            output.append(CharConstants.LINE_SEPARATOR)
                    .append(skip);
        }

        if (limit.length() > 0) {
            output.append(CharConstants.LINE_SEPARATOR)
                    .append(limit);
        }

        return output.toString();
    }

    public String generateInterval(final String from, final String to) {
        return new StringBuilder()
            .append(from)
            .append(CharConstants.LONG_DASH)
            .append(to)
            .toString();
    }

    private String generateTemporalFilterReturn(final List<ReturnEntity> returnEntities) {
        final StringBuilder sb = new StringBuilder();
        sb.append(CharConstants.SPACE)
          .append(YIELD)
          .append(CharConstants.SPACE)
          .append(ROW)
          .append(CharConstants.SPACE)
          .append(RETURN)
          .append(CharConstants.SPACE);

        int i = 0;
        for (final ReturnEntity r : returnEntities) {
            final String name = r.getId();
            sb.append(ROW)
              .append(CharConstants.POINT)
              .append(escape(name))
              .append(getAliasString(name));
            if (i != returnEntities.size() - 1) {
                sb.append(CharConstants.COMMA)
                  .append(CharConstants.SPACE);
            }
            i++;
        }
        return sb.toString();
    }

    private void generateFunctionCalls(final List<FunctionParams> functionParams) {
        final List<FunctionWith> withStructures = new LinkedList<>();
        functionParams.forEach(f -> {
            final FunctionWith withStructure = generateFunctionCall(f.getFunctionName(), f.getVariableName(), f.getBetweenPeriod(), f.getArgs());
            withStructures.add(withStructure);
            call.append(CharConstants.LINE_SEPARATOR);
        });
        generateWith(withStructures);
    }

    /**
     * Used for function calls in TempoGraph MATCH
     * @param function function name
     * @param variable variable assign to function return
     * @param args procedure params and configuration
     * @see FunctionParams
     * @return a structure used for yield output
     */
    private FunctionWith generateFunctionCall(final String function, final String variable, final Optional<Pair<String, String>> optional, final String[] args) {
        final String procedureName;
        final String[] functionReturn;
        switch (function.toLowerCase()) {
            case FUNCTION_EARLIEST:
                procedureName = PROCEDURE_EARLIEST;
                functionReturn = new String[]{PATH, INTERVAL};
                break;
            case FUNCTION_FASTEST:
                procedureName = PROCEDURE_FASTEST;
                functionReturn = new String[]{PATH, INTERVAL};
                break;
            case FUNCTION_SHORTEST:
                procedureName = PROCEDURE_SHORTEST;
                functionReturn = new String[]{PATH, INTERVAL};
                break;
            case FUNCTION_LATEST_DEPARTURE:
                procedureName = PROCEDURE_LATEST_DEPARTURE;
                functionReturn = new String[]{PATH, INTERVAL};
                break;
            case FUNCTION_LATEST_ARRIVAL:
                procedureName = PROCEDURE_LATEST_ARRIVAL;
                functionReturn = new String[]{PATH, INTERVAL};
                break;
            case FUNCTION_CPATH:
                procedureName = PROCEDURE_CPATH;
                functionReturn = new String[]{PATH, INTERVAL};
                break;
            case FUNCTION_CPATH2:
                procedureName = PROCEDURE_CPATH;
                functionReturn = new String[]{PATH, INTERVAL};
                break;
            case FUNCTION_CPATH_EXISTS:
                procedureName = PROCEDURE_CPATH_EXISTS;
                functionReturn = new String[]{VALUE.toLowerCase()};
                break;
            case FUNCTION_PAIR_CPATH:
                procedureName = PROCEDURE_PAIR_CPATH;
                functionReturn = new String[]{PATH, INTERVALS};
                break;
            case FUNCTION_PAIR_CPATH_EXISTS:
                procedureName = PROCEDURE_PAIR_CPATH_EXISTS;
                functionReturn = new String[]{VALUE.toLowerCase()};
                break;
            case FUNCTION_SENSOR_FLOWING:
                procedureName = PROCEDURE_SENSOR_FLOWING;
                functionReturn = new String[]{PATH, INTERVALS};
                break;  
            case FUNCTION_SENSOR_FLOWING_BACKWARDS:
                procedureName = PROCEDURE_SENSOR_FLOWING_BACKWARDS;
                functionReturn = new String[]{PATH, INTERVALS};
                break;  
            case FUNCTION_SENSOR_CPATH:
                procedureName = PROCEDURE_SENSOR_CPATH;
                functionReturn = new String[]{PATH, INTERVAL};
                break; 
            case FUNCTION_ALPHA:
                procedureName = PROCEDURE_ALPHA;
                functionReturn = new String[]{PATH, INTERVALS, ALPHAS};
                break; 
            default:
                throw new ParseCancellationException("Unrecognized function name");
        }

        /*if (!optional.isEmpty() && procedureName.equals(PROCEDURE_CPATH) && function.toLowerCase().equals(FUNCTION_CPATH)) {
            return generateWrappedInCache(function, variable, optional, procedureName, args, functionReturn);
        } else if (!optional.isEmpty() && procedureName.equals(PROCEDURE_CPATH) && function.toLowerCase().equals(FUNCTION_CPATH2)) { 
            return generateIndexBasedCPathQuery(function, variable, optional, procedureName, args, functionReturn);
        }]*/

        call.append(CALL)
            .append(CharConstants.SPACE)
            .append(procedureName)
            .append(CharConstants.OPENING_PARENTHESIS);

        for (int i = 0; i < args.length ; i++) {
            if (i != 0)
                call.append(CharConstants.COMMA);
            call.append(args[i]);
        }

        call.append(CharConstants.CLOSING_PARENTHESIS);
        return generateFunctionYield(variable, function, functionReturn);
    }

    private FunctionWith generateIndexBasedCPathQuery(String function, String variable,
        Optional<Pair<String, String>> betweenPeriodPair, String procedureName, String[] args, String[] fields) {
            if (this.preservedStateHelper.getIsIndexEnabled()) {
                generateGraphIndexCall(function, variable, betweenPeriodPair, args, RETRIEVE_PATHS, fields);
            } else {
                generateRegularFunctionCall(function, variable, betweenPeriodPair, args, procedureName, new String[]{PATH, INTERVAL});
            }
            FunctionWithStruct functionWith = new FunctionWithStruct(variable);

            for (final String field : fields) {
                final String structValue = field;
                functionWith.addPair(field, structValue);
            }

            return functionWith;
    }

    private FunctionWith generateWrappedInCache(String function, String variable,
            Optional<Pair<String, String>> betweenPeriodPair, String procedureName, String[] args, String[] fields) {
        generateGraphIndexCall(function, variable, betweenPeriodPair, args, RETRIEVE_PATHS_EXISTS, new String[]{VALUE.toLowerCase()});
        call.append(CharConstants.SPACE)
            .append(AS)
            .append(CharConstants.SPACE)
            .append(INDEX_CONDITION)
            .append(CharConstants.LINE_SEPARATOR);
        String apocYieldValueName = generateApocCall(function, variable, betweenPeriodPair, procedureName, args);
        
        // Generate FunctionWith instance
        FunctionWithStruct functionWith = new FunctionWithStruct(variable);

        for (final String field : fields) {
            final String structValue = apocYieldValueName + "." + field;
            functionWith.addPair(field, structValue);
        }

        return functionWith;
    }

    private String generateApocCall(String function, String variable, Optional<Pair<String, String>> betweenPeriodPair,
                                    String procedureName, String[] args) {
        call.append(CALL)
            .append(CharConstants.SPACE)
            .append(APOC_WHEN)
            .append(CharConstants.OPENING_PARENTHESIS)
            .append(INDEX_CONDITION)
            .append(CharConstants.COMMA)
            .append(CharConstants.LINE_SEPARATOR);

        // IF ELSE stmts part
        call.append(CharConstants.DOUBLE_QUOTE);
        String[] modifiedArgs = Arrays.copyOf(args, args.length);
        // These are node names
        modifiedArgs[0] = "_" + modifiedArgs[0];
        modifiedArgs[1] = "_" + modifiedArgs[1];

        generateGraphIndexCall(function, variable, betweenPeriodPair, modifiedArgs, RETRIEVE_PATHS, new String[]{PATH, INTERVAL});
        call.append(CharConstants.SPACE)
            .append(RETURN)
            .append(CharConstants.SPACE)
            .append(PATH)
            .append(CharConstants.COMMA)
            .append(INTERVAL);

        call.append(CharConstants.DOUBLE_QUOTE)
            .append(CharConstants.COMMA)
            .append(CharConstants.LINE_SEPARATOR);

        call.append(CharConstants.DOUBLE_QUOTE);
        generateRegularFunctionCall(function, variable, betweenPeriodPair, modifiedArgs, procedureName, new String[]{PATH, INTERVAL});
        call.append(CharConstants.SPACE)
            .append(RETURN)
            .append(CharConstants.SPACE)
            .append(PATH)
            .append(CharConstants.COMMA)
            .append(INTERVAL);
        call.append(CharConstants.DOUBLE_QUOTE);

        call.append(CharConstants.COMMA)
            .append(CharConstants.LINE_SEPARATOR);

        // Parameter for apoc.when with the dict with variable conversions
        call.append(CharConstants.OPENING_BRACKET)
            .append(modifiedArgs[0]).append(CharConstants.COLON).append(args[0])
            .append(CharConstants.COMMA)
            .append(modifiedArgs[1]).append(CharConstants.COLON).append(args[1])
            .append(CharConstants.CLOSING_BRACKET);
        call.append(CharConstants.CLOSING_PARENTHESIS).append(CharConstants.SPACE)
            .append(YIELD).append(CharConstants.SPACE).append(VALUE.toLowerCase()).append(CharConstants.SPACE).append(AS).append(CharConstants.SPACE);
        
        // Generate variable name for apoc yield (value)

        String yieldValueName = parserVariablesGenerator.createVariable(VALUE.toLowerCase());
        call.append(yieldValueName).append(CharConstants.LINE_SEPARATOR);
        
        return yieldValueName;
    }

    private void generateRegularFunctionCall(String function, String variable,
    Optional<Pair<String, String>> betweenPeriodPair, String[] args, String procedureName, String[] yieldVariables) {
        call.append(CALL)
            .append(CharConstants.SPACE)
            .append(procedureName)
            .append(CharConstants.OPENING_PARENTHESIS);

        for (int i = 0; i < args.length ; i++) {
            if (i != 0)
                call.append(CharConstants.COMMA);
            call.append(args[i]);
        }
        call.append(CharConstants.CLOSING_PARENTHESIS);
        // Yield part
        call.append(CharConstants.SPACE).append(YIELD).append(CharConstants.SPACE);
        for (int i = 0; i < yieldVariables.length; i++) {
            if (i != 0)
                call.append(CharConstants.COMMA);
            call.append(yieldVariables[i]);
        }
    }

    private void generateGraphIndexCall(String function, String variable,
                                        Optional<Pair<String, String>> betweenPeriodPair, String[] args, String indexFunctionName, String[] yieldVariables) {
        call.append(CALL)
        .append(CharConstants.SPACE)
        .append(GRAPHINDEX)
        .append(CharConstants.POINT)
        .append(indexFunctionName)
        .append(CharConstants.OPENING_PARENTHESIS);
        // Appending params to 
        for (int i = 0; i < 4; i++) {
            call.append(args[i])
                .append(CharConstants.COMMA);
        }
        Pair<String, String> betweenPeriod = betweenPeriodPair.get();
            call.append(args[4])
            .append(CharConstants.CLOSING_PARENTHESIS);

        // Yield part
        call.append(CharConstants.SPACE).append(YIELD).append(CharConstants.SPACE);
        for (int i = 0; i < yieldVariables.length; i++) {
            if (i != 0)
                call.append(CharConstants.COMMA);
            call.append(yieldVariables[i]);
        }
    }


    /**
     * Used to generate and append the YIELD definition to a function call
     * All functions to date return something (no void calls)
     * @param variable variable assign to function return
     * @param fields fields returned by the procedure
     * @return the function statement used for WITH
     */
    public FunctionWith generateFunctionYield(final String variable, final String function, final String... fields) {
        call.append(CharConstants.SPACE + YIELD + CharConstants.SPACE);
        if (fields.length > 1)
            return generateFunctionStructYield(variable, function, fields);
        else
            return generateFunctionVariableYield(variable, fields[0]);
    }

    /**
     * Yield for functions that generate a struct statement in YIELD (used for procedures with more than two return fields)
     */
    private FunctionWith generateFunctionStructYield(final String callVariable, final String function, final String... fields) {
        int i = 0;
        FunctionWithStruct functionWith = new FunctionWithStruct(callVariable);
        functionWith.setFunction(function);
        for (final String field : fields) {
            final String variable = parserVariablesGenerator.createVariable(field);
            call.append(field)
                    .append(CharConstants.SPACE)
                    .append(AS)
                    .append(CharConstants.SPACE)
                    .append(variable);
            functionWith.addPair(field, variable);
            if (i != fields.length - 1) {
                call.append(CharConstants.COMMA)
                        .append(CharConstants.SPACE);
            }
            i++;
        }
        return functionWith;
    }

    /**
     * used for procedures with a single return field
     */
    private FunctionWith generateFunctionVariableYield(final String callVariable, final String field) {
        call.append(field)
                .append(CharConstants.SPACE)
                .append(AS)
                .append(CharConstants.SPACE)
                .append(callVariable);
        return new FunctionWith(callVariable);
    }
    /**
     * Used to generate and append the WITH definition to a function call
     * @param withStructures structures for each variable
     */
    private void generateWith(final List<FunctionWith> withStructures) {
        List<FunctionWithStruct> unwindVariables = new LinkedList<>();
        call.append(WITH + CharConstants.SPACE);
        int i = 0;
        for (final FunctionWith withStruct : withStructures) {
            call.append(withStruct.toString());
            if (i != withStructures.size() - 1) {
                call.append(CharConstants.COMMA);
                call.append(CharConstants.SPACE);
            }
            i++;
            if (withStruct.unwindable()) {
                unwindVariables.add((FunctionWithStruct) withStruct);
            }
        }
        FunctionParams.getSelectedVariables().forEach(v -> {
            call.append(CharConstants.COMMA);
            call.append(CharConstants.SPACE);
            call.append(v);
        });
        call.append(CharConstants.LINE_SEPARATOR);
        for (final FunctionWithStruct withStruct: unwindVariables) {
            call.append(withStruct.unwindString());
        }
    }

    public QueryState getState() {
        return this.state;
    }

    public void setState(QueryState state) {
        this.state = state;
    }

    void appendPartOfPathToMatch(String s) {
        switch (this.state) {
            case MATCH:
                this.matchAppend(s);
                break;
            case WHEN:
                this.whenMatchAppend(s);
                break;
        }
    }

    /**
     * Generates a cypher query for a WHEN TempoGraph query
     * @param whenParams the parameters of the query
     * @return a cypher query
     */
    String generateWhenQuery(TemporalFilterParams whenParams) {
        final ReturnEntitiesJson returnEntitiesJson = new ReturnEntitiesJson(whenParams.getReturnEntities());

        String implicitTemporalQuery = generateWhenImplicitTemporalQuery();

        String unwind = UNWIND +  " intervals as interval\n";

        String betweenCall = generateTemporalFilterCall(PROCEDURE_BETWEEN,  whenParams.getReturnEntities(), TEMPORAL_OP_PRETTY_PRINTING, new FunctionArgument(escapeQuery(whenParams.getQuery())),
                new FunctionArgument(whenParams.getValue(), false), new FunctionArgument(jsonWithoutQuotesInKeys(returnEntitiesJson), false));

        return implicitTemporalQuery + unwind + betweenCall;
    }

    /**
     * Used for when clause
     * Generates the inner query that will get the implicit interval for the main or outer query
     * @return a query that returns an interval
     */
    // TODO capture interval for cache
    private String generateWhenImplicitTemporalQuery() {
        StringBuilder intervalQuery = new StringBuilder()
                .append(MATCH)
                .append(CharConstants.SPACE)
                .append(whenMatch);

        if (whenWhere.length() > 0)
            intervalQuery
                    .append(WHERE)
                    .append(CharConstants.SPACE)
                    .append(whenWhere)
                    .append(CharConstants.LINE_SEPARATOR);

        intervalQuery.append(WITH)
                     .append(CharConstants.SPACE)
                     .append(DISTINCT)
                     .append(CharConstants.SPACE)
                     .append(whenEdge)
                     .append("." + INTERVAL +" as intervals\n");

        return intervalQuery.toString();
    }

    public static String getAliasString(final String alias) {
        return CharConstants.SPACE + AS + CharConstants.SPACE + escape(alias);
    }

    public static String escape(String text) {
        return CharConstants.GRAVE_ACCENT + text + CharConstants.GRAVE_ACCENT;
    }

    public void whenWhereAppend(String text) {
        if (whenWhere.length() > 0) {
            whenWhere.append(AND);
        }
        whenWhere.append(text);
    }

    public void whenMatchAppend(String text) {
        whenMatch.append(text);
    }

    public void limitAppend(String text) {
        limit.append(text);
    }

    public void skipAppend(String text) {
        skip.append(text);
    }

    public void addPath(final Set<String> objectVariables, final String definition) {
        matchPaths.add(new Path(objectVariables, definition));
    }

    public List<Path> getPaths(final String variable) {
        return matchPaths.stream().filter(p -> !p.isAddedToWhen() && p.getObjectVariables().contains(variable)).collect(Collectors.toList());
    }

    /**
     * Used for when clause
     * Get the attributes expanded by an object. Only considers those attributes found in where
     * @return a list of definitions or paths
     */
    List<String> expandedAttributes(final String object) {
        if (!variableAttributes.containsKey(object) || variableAttributes.get(object).getAttributes().isEmpty())
            return new LinkedList<>();
        return variableAttributes.get(object).getAttributes().values().stream().map(Attribute::getValue)
                .filter(v -> !v.foundInSelect())
                .map(v -> MATCH + CharConstants.SPACE + v.getDefinition() + CharConstants.LINE_SEPARATOR).collect(Collectors.toList());
    }

    /**
     * Sets the only edge of when
     * @param edgeVariable edge variable
     */
    public void setWhenEdge(String edgeVariable) {
        this.whenEdge = edgeVariable;
    }

    public ParserVariables getParserVariablesGenerator() {
        return parserVariablesGenerator;
    }

    public boolean functionWhereIsEmpty() {
        return functionWhere.length() == 0;
    }

    public void appendFunctionWhere(final String s) {
        functionWhere.append(s);
    }

    /**
     * @param function name of function
     * @return if the function name is a cpath or pair cpath or boolean call
     */
    public boolean isCpathFunction(String function) {
        return function.toLowerCase().equals(FUNCTION_CPATH) 
        		|| function.toLowerCase().equals(FUNCTION_PAIR_CPATH)
                || function.toLowerCase().equals(FUNCTION_CPATH_EXISTS) 
                || function.toLowerCase().equals(FUNCTION_PAIR_CPATH_EXISTS)
                || function.toLowerCase().equals(FUNCTION_CPATH2) 
                || function.toLowerCase().equals(FUNCTION_SENSOR_FLOWING_BACKWARDS) 
                || function.toLowerCase().equals(FUNCTION_SENSOR_FLOWING)
                || function.toLowerCase().equals(FUNCTION_ALPHA)
                || function.toLowerCase().equals(FUNCTION_SENSOR_CPATH);
    }

    public List<String> generateCreateQuery(String relationshipName, Integer maxLength, String fromDate, String toDate, Integer maxId, IndexType indexType) {
        List<String> queries = new ArrayList<>();
        int maxxId = maxId;
        for (int i = 0; i < maxxId; i+= INDEX_OBJECTS_PER_BLOCK) {
            StringBuilder output = new StringBuilder();
            output.append(CALL).append(CharConstants.SPACE).append(GRAPHINDEX).append(CharConstants.POINT);
            switch (indexType) {
                case GRAPH:
                    output.append(CREATE_INDEX);
                break;
                case TREE:
                    output.append(CREATE_TREE_INDEX);
                break;
            }  
                output.append(CharConstants.OPENING_PARENTHESIS)
                    .append(relationshipName)
                    .append(CharConstants.COMMA);
                if(indexType == IndexType.TREE) {
                    output.append(maxLength)
                    .append(CharConstants.COMMA);
                }
                output.append(fromDate)
                    .append(CharConstants.COMMA)
                    .append(toDate)
                    .append(CharConstants.COMMA)
                    .append(i)
                    .append(CharConstants.COMMA)
                    .append(i+INDEX_OBJECTS_PER_BLOCK)
                .append(CharConstants.CLOSING_PARENTHESIS)
                .append(CharConstants.SEMICOLON);
            queries.add(output.toString());        
        }
        return queries;
    }

    public List<String> generateConnectQuery(String relationshipName, String fromDate, String toDate, Integer minId, Integer maxId) {
        List<String> queries = new ArrayList<>();

        for (int i1 = minId; i1 <= maxId; i1 += INDEX_OBJECTS_PER_BLOCK) {
            for (int i2 = minId; i2 <= maxId; i2 += INDEX_CONNECTIONS_PER_BLOCK) {
                StringBuilder output = new StringBuilder();
                output.append(CALL).append(CharConstants.SPACE).append(GRAPHINDEX).append(CharConstants.POINT).append(CONNECT_INDEX)
                .append(CharConstants.OPENING_PARENTHESIS)
                    .append(relationshipName)
                    .append(CharConstants.COMMA)
                    .append(fromDate)
                    .append(CharConstants.COMMA)
                    .append(toDate)
                    .append(CharConstants.COMMA)
                    .append(i1)
                    .append(CharConstants.COMMA)
                    .append(i1+INDEX_OBJECTS_PER_BLOCK)
                    .append(CharConstants.COMMA)
                    .append(i2)
                    .append(CharConstants.COMMA)
                    .append(i2+INDEX_CONNECTIONS_PER_BLOCK)
                .append(CharConstants.CLOSING_PARENTHESIS)
                .append(CharConstants.SEMICOLON);
            queries.add(output.toString());        
            }
        }

        return queries;
    }


    public String generateCreateOrUpdateQuery(CreateOrUpdateQuery createOrUpdateQuery) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("MATCH ").append(createOrUpdateQuery.getObjectDef1())
        .append("\nMATCH ").append(createOrUpdateQuery.getObjectDef2())
        .append("\nWHERE ").append(where)
        .append("\nCALL ").append(GRAPHINDEX).append(CharConstants.POINT);
        switch (createOrUpdateQuery.getIndexType()) {
            case GRAPH:
                queryBuilder.append(GRAPH_CREATE_OR_UPDATE);
            break;
            case TREE:
                queryBuilder.append(TREE_CREATE_OR_UPDATE);
            break;
            default:
            break;
        }
        queryBuilder.append(CharConstants.OPENING_PARENTHESIS)
        .append(createOrUpdateQuery.getObjectVariable1())
        .append(CharConstants.COMMA)
        .append(createOrUpdateQuery.getObjectVariable2())
        .append(CharConstants.COMMA)
        .append(CharConstants.QUOTE)
        .append(createOrUpdateQuery.getRelationshipType())
        .append(CharConstants.QUOTE)
        .append(CharConstants.CLOSING_PARENTHESIS)
        .append("\n RETURN 'All Done'");
        return queryBuilder.toString();
    }


    public String deleteQuery(DeleteQuery deleteQuery) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("MATCH ").append(deleteQuery.getObjectDefinition1())
        .append(" - [").append(deleteQuery.getRelationshipObjectName2())
            .append(":").append(deleteQuery.getRelationshipType())
            .append("] -> ")
        .append(deleteQuery.getObjectDefinition2())
        .append("\nWHERE ").append(where)
        .append("\nCALL ").append(GRAPHINDEX).append(CharConstants.POINT);
        switch (deleteQuery.getIndexType()) {
            case GRAPH:
                queryBuilder.append(GRAPH_DELETE);
            break;
            case TREE:
                queryBuilder.append(TREE_DELETE);
            break;
            default:
            break;
        }
        queryBuilder.append(CharConstants.OPENING_PARENTHESIS)
        .append(deleteQuery.getRelationshipObjectName())
        .append(CharConstants.CLOSING_PARENTHESIS)
        .append("\n RETURN 'All Done'");

        return queryBuilder.toString();
    }


    public List<String> generateDeleteAllIndicesQuery(Integer minId, Integer maxId) {
        return Collections.singletonList("CALL graphindex.deleteAllIndices();");
    }


    public List<String> generateDeleteGraphIndex(String relationship) {
        return Collections.singletonList("CALL graphindex.deleteGraphIndex("+relationship+");");
    }


    public List<String> generateDeleteTreeIndex(String relationship, String fromDate, String toDate, Integer minId,
            Integer maxId) {
        return Collections.singletonList("CALL graphindex.deleteTreeIndex("+relationship+", "+fromDate+", "+toDate+");");
    }
}
