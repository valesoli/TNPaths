package ar.edu.itba.grammar;

import ar.edu.itba.grammar.struct.*;
import ar.edu.itba.grammar.struct.params.FunctionParams;
import ar.edu.itba.util.Pair;
import ar.edu.itba.util.Time;
import ar.edu.itba.util.Utils;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class GrammarListenerHelper {

    private final Map<String, Variable> variableAttributes;

    private final CypherOutputGenerator cyOut;

    private final AtomicInteger selectIndex;

    private static final String NULL_STRING = "null";

    private CacheableContent cacheableContent;

    public GrammarListenerHelper(final Map<String, Variable> variableAttributes, final CypherOutputGenerator cyOut,
                                 final AtomicInteger selectIndex, final CacheableContent cacheableContent) {
        this.variableAttributes = variableAttributes;
        this.cyOut = cyOut;
        this.selectIndex = selectIndex;
        this.cacheableContent = cacheableContent;
    }

    /**
     * Called when an attribute is found in select or where
     * @param variable object variable
     * @param attribute attribute string
     * @param selectAttribute attribute found in select or where statements
     * @param aliasCtx if there is an alias present (select case)
     * @return the value node variable of the corresponding attribute
     */
    public String attributeFound(final String variable, final String attribute, final boolean selectAttribute,
                                  final Optional<TempoGraphParser.AliasContext> aliasCtx, final Set<String> whenEdgesVariables) {

        final Variable v = variableAttributes.getOrDefault(variable, new Variable(variable));
        variableAttributes.put(variable, v);

        if (!v.hasAttribute(attribute)) {
            final String valueNodeVariable;
            final String definition;
            if (!aliasCtx.isPresent()) {
                valueNodeVariable = cyOut.getParserVariablesGenerator().createVariable(CypherOutputGenerator.VALUE);
                definition = cyOut.createPath(variable, attribute, valueNodeVariable);
            }
            else {
                valueNodeVariable = aliasCtx.get().WORD().getText();
                definition = cyOut.createPath(variable, attribute, valueNodeVariable);
            }
            final Attribute newAttribute = new Attribute(v, attribute, valueNodeVariable, definition, selectAttribute);
            if (selectAttribute)
                newAttribute.getValue().setSelectIndex(selectIndex.getAndIncrement());
            v.addAttribute(newAttribute);
            if (cyOut.getState() == QueryState.WHEN && !whenEdgesVariables.contains(variable))
                cyOut.whenAppendDefinition(definition);
            return valueNodeVariable;
        } else {
            final Value value = v.getAttribute(attribute).getValue();
            value.setFoundInSelect(selectAttribute);
            if (selectAttribute) {
                if (!value.isSelected())
                    value.setSelectIndex(selectIndex.getAndIncrement());
                else
                    throw new ParseCancellationException("Attribute " + attribute + " of variable " + variable + " is already selected");
            }
        }
        return v.getAttribute(attribute).getValue().getName();
    }

    /**
     * Called to get all the variables involved in a condition
     * @param valueContext right or left side of a where condition
     * @param conditionVariables set to append the variables found
     * @param aliasesVariable map alias to corresponding object variable
     * @param aliasesInWhere aliases found in where as tokens
     */
    public void addConditionVariables(final TempoGraphParser.ValueContext valueContext, final Set<String> conditionVariables,
                                      final Map<String, String> aliasesVariable, final Set<Token> aliasesInWhere) {
        if (valueContext.math() == null)
            return;
        addConditionVariable(valueContext.math(), conditionVariables, aliasesVariable, aliasesInWhere);
    }

    /**
     * Called recursively to find all the math contexts children
     */
    private void addConditionVariable(final TempoGraphParser.MathContext ctx, final Set<String> conditionVariables,
                                      final Map<String, String> aliasesVariable, final Set<Token> aliasesInWhere) {

        if (!ctx.math().isEmpty()) {
            for (TempoGraphParser.MathContext m : ctx.math())
                addConditionVariable(m, conditionVariables, aliasesVariable, aliasesInWhere);
            return;
        }

        //skip numerical values
        if (ctx.atom().scientific() != null)
            return;

        final String variable;
        //alias found
        if (ctx.atom().WORD() != null) {
            final String alias = ctx.atom().WORD().getText();
            if (!aliasesVariable.containsKey(alias))
                throw new ParseCancellationException(Utils.generateErrorMessage("alias " + alias + " undefined", ctx.atom().WORD().getSymbol()));
            variable = aliasesVariable.get(alias);
            aliasesInWhere.add(ctx.atom().WORD().getSymbol());
        }
        else if (ctx.atom().attr() != null) {
            variable = ctx.atom().attr().WORD(0).getText();
            String attribute = ctx.atom().attr().WORD(1).getText();
            if (cyOut.getState() == QueryState.MATCH) {
                cacheableContent.addMatchAttributeForVariable(variable, attribute);
            } else if (cyOut.getState() == QueryState.WHEN) {
                cacheableContent.addWhenWhereAttributeForVariable(variable, attribute);
            }
        }
        else if (ctx.atom().id_call() != null)
            variable = ctx.atom().id_call().WORD().getText();
        else {
            final TerminalNode propertyAccess = ctx.atom().property_access().ID() != null ? ctx.atom().property_access().ID() : ctx.atom().property_access().WORD(1);
            variable = propertyAccess.getText();
        }
        conditionVariables.add(variable);
    }

    /**
     * Called to generate function parameters configuration
     * @param function function name
     * @param functionVariableName variable assign to function return
     * @param path path argument of function
     * @return function parameters
     */
    public FunctionParams.Builder handleFunction(final String function, final String functionVariableName, final TempoGraphParser.Endpoints_argsContext path) {
        final String initialNode = path.object().get(0).object_variable().WORD().getText();

        final ProcedureConfiguration configuration = new ProcedureConfiguration(path);

        boolean hasEndingNode = path.object().size() == 2 &&
                path.object().get(1).object_variable() != null;

        final FunctionParams.Builder functionParamsBuilder = new FunctionParams.Builder(function, functionVariableName, initialNode,
                (hasEndingNode ? path.object().get(1).object_variable().WORD().getText() : NULL_STRING), configuration);

        final String offset = "1";
        boolean isCPath = cyOut.isCpathFunction(function);
        if (hasEndingNode && !isCPath)
            functionParamsBuilder.withOffset(offset);
        else {
            functionParamsBuilder.withMinLength(configuration.getMinLength().toString());
            functionParamsBuilder.withMaxLength(configuration.getMaxLength().toString());
        }
        return functionParamsBuilder;
    }

    /**
     * Called to get a date interval
     * @param from from date in string format
     * @param to to date in string format
     * @param invalidIntervalMessage error message in case the interval is invalid
     * @return string representation of date interval argument
     */
    public Pair<String, String> getTimeInterval(final String from, final String to, final String invalidIntervalMessage) {
        if (!Time.validateTimeInterval(from, to))
                throw new ParseCancellationException(invalidIntervalMessage);
        return new Pair<>(from, to);
    }

    /**
     * Appends the attributes of non function variables to match
     * Append selected attributes to cypher return and to return entities
     * @param variable variable string
     * @param attributes expanded for variable
     * @param functionVariables variables defined for functions
     * @param returnEntities used for temporal operators. An integer is used as index to order the items to match user return order
     * @param returnExpr expressions to be appended to return. An integer is used as index to order the items to match user return order
     */
    public void addAttributesForVariable(final String variable, final Collection<Attribute> attributes, final Set<String> functionVariables,
            final Set<String> edgesVariables, final Map<Integer, ReturnEntity> returnEntities, final Map<Integer, String> returnExpr) {
        for (final Attribute attribute : attributes) {
            final Value value = attribute.getValue();
            if (notContained(variable, functionVariables, edgesVariables))
                cyOut.matchAppendDefinition(value.getDefinition(), value.foundInSelect());
            //append attributes to return and fill return entities
            value.appendToReturn(functionVariables, edgesVariables, returnEntities, returnExpr);
        }
    }

    /**
     * @param returnEntities used for temporal operators
     * @return return entities ordered by index
     */
    public List<ReturnEntity> orderReturnEntities(final Map<Integer, ReturnEntity> returnEntities) {
        return new LinkedList<>(returnEntities.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new))
                .values());
    }

    /**
     * @param returnExpr expressions to be appended to return
     */
    public void appendAndOrderReturnExpr(final Map<Integer, String> returnExpr) {
        returnExpr.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> cyOut.retAppend(e.getValue()));
    }

    /**
     * Appends object definitions, attributes expanded and where conditions to when clause when necessary
     * @param whereConditions all the inner and outer conditions found in the query
     * @return where conditions to append
     */
    public Set<String> handleConditionsForWhen(final List<BaseWhereCondition> whereConditions, final Set<String> whenVariables) {
        for (final WhenWhereCondition c : getWhenWhereConditions(whereConditions)) {
            final Set<String> missingDefinitions = c.getMissingVariables(whenVariables);
            if (!missingDefinitions.isEmpty())
                addObjectVariablesToWhen(missingDefinitions, whenVariables);
        }

        final Set<String> conditions = new HashSet<>();
        for (final WhereCondition c : getWhereConditions(whereConditions)) {
            if (c.containedIn(whenVariables))
                    conditions.add(c.getCondition());
        }
        return conditions;
    }

    /**
     * @param whereConditions where conditions found in query
     * @return where conditions found in when
     */
    public Collection<WhenWhereCondition> getWhenWhereConditions(final List<BaseWhereCondition> whereConditions) {
        return whereConditions.stream().filter(c -> c instanceof  WhenWhereCondition)
                .map(c -> (WhenWhereCondition) c).collect(Collectors.toList());
    }

    /**
     * @param whereConditions where conditions found in query
     * @return where conditions found in where
     */
    public Collection<WhereCondition> getWhereConditions(final List<BaseWhereCondition> whereConditions) {
        return whereConditions.stream().filter(c -> c instanceof  WhereCondition)
                .map(c -> (WhereCondition) c).collect(Collectors.toList());
    }

    /**
     * Adds match paths and expanded attributes for missing variable
     * @param missingObjectVariables missing variables definitions (references to outer query)
     * @param whenVariables variables defined in when by the user or already added by the parser
     */
    public void addObjectVariablesToWhen(final Set<String> missingObjectVariables, final Set<String> whenVariables) {
        if (missingObjectVariables.isEmpty())
            return;
        final Set<String> newVariables = new HashSet<>();
        do {
            newVariables.clear();
            for (final String missingVariable : missingObjectVariables) {
                whenVariables.add(missingVariable);
                List<Path> missingPaths = cyOut.getPaths(missingVariable);
                missingPaths.forEach(p -> {
                    p.setAddedToWhen(true);
                    cyOut.whenAppendDefinition(p.getDefinition());
                    newVariables.addAll(p.getObjectVariables().stream().filter(v -> !whenVariables.contains(v))
                    .collect(Collectors.toList()));
                });
            }
            appendExpandedAttributesToWhenMatch(missingObjectVariables);
            missingObjectVariables.clear();
            missingObjectVariables.addAll(newVariables);
        } while (!newVariables.isEmpty());
    }

    /**
     * Appends all the expanded attributes for each variable reference to when match
     * @param whenVariablesReferences variables in when match that reference to the outer query match variables
     */
    public void appendExpandedAttributesToWhenMatch(final Set<String> whenVariablesReferences) {
        whenVariablesReferences.stream()
                .map(cyOut::expandedAttributes)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(cyOut::whenMatchAppend);
    }

    /**
     * Called for edges and objects nodes when SELECT * is used
     * Set selected: true for any node found in MATCH
     * If the user variable is missing the parser generates a variable to return
     * @param words contains the user variable if there is any and the node label
     * @param variable variable already found
     * @param parserVariables variables defined by parser
     * @param colonNode colon terminal node to append the variable generated by the parser
     * @return the user variable or the variable generated by the parser
     */
    public String nodeSelectAll(final List<TerminalNode> words, String variable, final Set<String> parserVariables,
                              final TerminalNode colonNode) {
        if (!variableDefinition(words)) {
            variable = generateVariableForDefinition(words.get(0).getText(), colonNode);
            parserVariables.add(variable);
        }
        final Variable v = new Variable(variable);
        variableAttributes.put(variable, v);
        v.setSelectIndex(selectIndex.getAndIncrement());
        return variable;
    }

    /**
     * Called to generate and append a variable to a node definition
     * @param label node label
     * @param colonNode colon terminal node to append the variable generated by the parser
     * @return an internal parser variable
     */
    public String generateVariableForDefinition(final String label, final TerminalNode colonNode) {
        final String variable = cyOut.getParserVariablesGenerator().createVariable(label);
        CommonToken colon = (CommonToken) colonNode.getSymbol();
        colon.setText(variable + colon.getText());
        return variable;
    }

    /**
     * @param ctx path context
     * @return the object variables contained in the user path
     */
    public Set<String> getObjectVariablesFromPath(final TempoGraphParser.PathContext ctx) {
        final Set<String> variableDefinitions = ctx.object().stream().filter(o -> o.object_def() != null && variableDefinition(o.object_def().WORD()))
                .map(o -> o.object_def().WORD(0).getText()).collect(Collectors.toSet());
        final Set<String> variableReferences = ctx.object().stream().filter(o -> o.object_variable() != null)
                .map(o -> o.object_variable().WORD().getText()).collect(Collectors.toSet());
        variableDefinitions.addAll(variableReferences);
        return variableDefinitions;
    }

    public void validateDefinitions(String prefixError, String postfixError, Optional<Token> token, boolean containsAny,
                                    Collection<String> definitions, Collection<String>... collections) {
        definitions.forEach(d -> validateDefinition(prefixError, postfixError, d, containsAny, token, collections));
    }

    /**
     * throws an exception if the definition is not valid
     * @param prefixError prefix of the error message used in the exception
     * @param postfixError postfix of the error message used in the exception
     * @param definition definition to validate. Used as part of the error message used in the exception
     * @param containsAny validation criteria. Contained in any collection or not contained at all
     * @param token used to show line error and position
     * @param collections collections to check against
     */
    public void validateDefinition(String prefixError, String postfixError, String definition,
                                   boolean containsAny, Optional<Token> token, Collection<String>... collections) {
        if (containsAny ? containsAny(definition, collections) : notContained(definition, collections)) {
            String message = prefixError + CharConstants.SPACE + definition + CharConstants.SPACE + postfixError;
            throw new ParseCancellationException(token.map(value -> Utils.generateErrorMessage(message, value)).orElse(message));
        }
    }

    /**
     * @param definition definition to validate. Used as part of the error message used in the exception
     * @param collections collections to check against
     * @return true if definition is contained in any collection
     */
    public boolean containsAny(final String definition, final Collection<String>... collections) {
        for (Collection<String> c : collections) {
            if (c.contains(definition))
                return true;
        }
        return false;
    }

    /**
     * @param definition definition to validate. Used as part of the error message used in the exception
     * @param collections collections to check against
     * @return true if definition is not contained in any collection
     */
    public boolean notContained(final String definition, final Collection<String>... collections) {
        for (Collection<String> c : collections) {
            if (c.contains(definition)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates the path argument of function calls
     * @param functionName function name
     * @param ctx parser context that contains the path argument
     * @param fullPath whether it has to validate a full path (cpath case, variable length and label definitions allowed) or a simpler path
     */
    public void validatePathArg(final String functionName, final Set<String> objectVariables,
                                final TempoGraphParser.Endpoints_argsContext ctx, boolean fullPath) {
        noVariablesDefinition(functionName, ctx);
        initialNode(functionName, ctx);
        if (fullPath) {
            maxLabelsDefinition(functionName, ctx, 1);
            asteriskWithLimits(functionName, ctx);
        }
        else {
            maxLabelsDefinition(functionName, ctx, 0);
            asteriskWithoutLimits(functionName, ctx);
        }
    }

    /**
     * Validates that the given path has an initial node
     * @param functionName function name
     * @param ctx parser context that contains the path argument
     */
    private void initialNode(final String functionName, final TempoGraphParser.Endpoints_argsContext ctx) {
        if (ctx.object(0).object_variable() == null)
            throw new ParseCancellationException("Function " + functionName + " must have an initial node reference");
    }

    /**
     * Validates the references of the function path argument
     * @param objectVariables valid references
     * @param ctx parser context that contains the path argument
     */
    public void validateEndpoints(final Set<String> objectVariables, final TempoGraphParser.Endpoints_argsContext ctx) {
        ctx.object().stream().map(TempoGraphParser.ObjectContext::object_variable).filter(Objects::nonNull)
                .forEach(o -> {
                    if (!objectVariables.contains(o.WORD().getText()))
                        throw new ParseCancellationException(Utils.generateErrorMessage("variable " + o.WORD().getText() + " undefined. Functions path argument endpoints must be defined.", o.WORD().getSymbol()));

                });
    }

    /**
     * Validates that the given path doesnt have variable definitions
     * @param functionName function name
     * @param ctx parser context that contains the path argument
     */
    private void noVariablesDefinition(final String functionName, final TempoGraphParser.Endpoints_argsContext ctx) {
        if (ctx.object().stream().map(TempoGraphParser.ObjectContext::object_def).filter(Objects::nonNull)
                .anyMatch(o -> variableDefinition(o.WORD())))
            throw new ParseCancellationException(functionName + " doesnt support variables definitions");
    }

    /**
     * Validates that the given path has at most max labels definitions
     * @param functionName function name
     * @param ctx parser context that contains the path argument
     * @param max max labels definitions
     */
    private void maxLabelsDefinition(final String functionName, final TempoGraphParser.Endpoints_argsContext ctx,
                                     final int max) {
        if (ctx.object().stream().map(TempoGraphParser.ObjectContext::object_def).filter(Objects::nonNull).count() > max) {
            final String errorMessage = (max == 0) ? " doesnt support label definitions" : " supports at most " + max + " label definitions";
            throw new ParseCancellationException(functionName + errorMessage);
        }
    }

    /**
     * Validates that the given path variable length is limited
     * @param functionName function name
     * @param ctx parser context that contains the path argument
     */
    private void asteriskWithLimits(final String functionName, final TempoGraphParser.Endpoints_argsContext ctx) {
        if (ctx.relation_edge().edge_def().variable_length() != null &&
                ctx.relation_edge().edge_def().variable_length().U_INTEGER().size() == 0)
            throw new ParseCancellationException(functionName + " doesnt support unlimited variable length, please use numerical value limiters");
    }

    /**
     * Validates that the given path length isn't limited
     * @param functionName function name
     * @param ctx parser context that contains the path argument
     */
    private void asteriskWithoutLimits(final String functionName, final TempoGraphParser.Endpoints_argsContext ctx) {
        if (ctx.relation_edge().edge_def().variable_length() == null ||
                ctx.relation_edge().edge_def().variable_length().U_INTEGER().size() != 0)
            throw new ParseCancellationException(functionName + " doesnt support limited variable length");
    }

    /**
     * @param word list belonging to context under validation
     * @return if the context has a variable definition
     */
    public boolean variableDefinition(final List<TerminalNode> word) {
        return word.size() == 2;
    }

    /**
     * @param ctx parser context that contains the time value
     * @return the token that is used to print error message line and position
     */
    public Token getTimeToken(TempoGraphParser.Time_valueContext ctx) {
        if (ctx.YEAR() != null)
            return ctx.YEAR().getSymbol();
        else if (ctx.YEAR_MONTH() != null)
            return ctx.YEAR_MONTH().getSymbol();
        else if (ctx.DATE() != null)
            return ctx.DATE().getSymbol();
        else
            return ctx.DATETIME().getSymbol();
    }
    
}
